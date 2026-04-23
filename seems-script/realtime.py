#!/usr/bin/env python3
# _*_ coding:utf-8 _*_
import asyncio
import json
import time
import random
import threading
import requests

from websocket import WebSocketApp
from handler.voyage_handler import REDIS_KEY_PREFIX as VOYAGE_KEY_PREFIX
import db
from db.sql.snowflakeId import IdWorker
from encoder import JSONEncoder
from alarm_client import get_alarm_client, init_alarm_client

WINDOW_SIZE = 10  # 设置数据缓冲窗口大小，最多保存最新的10条数据

worker = IdWorker(1, 2, 0)  # 使用雪花算法生成唯一ID，worker为ID生成器实例


class DataWebSocket:
    def __init__(self, args, position_cache=None):
        """
        WebSocket 数据处理类的初始化方法
        :param args: 输入的参数（例如ship_id、interval等）
        :param position_cache: 船舶位置缓存对象（ShipDataCache）
        """
        self.args = args
        self.position_cache = position_cache  # 位置缓存对象
        self.gt2_url = 'wss://server.monitor.epops.purvar.com/api/ws/real?ship_id=yungang_diantuo2'  # WebSocket的URL
        self.ws = None  # WebSocket实例
        self.tmp_msg_list = []  # 临时消息列表，用于存储接收到的消息
        self.msg_list = []  # 消息列表，用于存储处理过的消息
        self.last_time = time.time()  # 记录上一次操作的时间戳
        self.engine = db.SQLEngine(self.args['mysql']['host'], self.args['mysql']['port'], self.args['mysql']['user'], self.args['mysql']['password'], self.args['mysql']['database'])  # 数据库连接
        self.redis = db.RedisEngine(self.args['redis']['host'], self.args['redis']['port'], self.args['redis']['db'],self.args['redis']['password'])  # Redis连接
        self.on_message_handler = []  # 存储用户注册的消息处理函数

        # 后端API配置
        self.backend_api_url = self.args.get('backend_api', {}).get('url', 'http://localhost:8080')  # 后端API地址
        self.backend_api_timeout = self.args.get('backend_api', {}).get('timeout', 5)  # API超时时间

        # 报警检测配置
        self.soc_threshold = self.args.get('alarm', {}).get('soc_threshold', 15.0)  # SOC报警阈值（百分比）
        self.last_alarm_time = {}  # 记录上次报警时间，避免频繁报警 {ship_id: timestamp}

        # 报警策略配置
        self.use_alarm_strategy = self.args.get('alarm', {}).get('use_strategy', True)  # 是否使用策略配置
        self.default_soc_threshold = self.args.get('alarm', {}).get('soc_threshold', 15.0)  # 默认SOC阈值
        self.default_temp_threshold = self.args.get('alarm', {}).get('temp_threshold', 60.0)  # 默认温度阈值

        # 初始化报警客户端
        if self.use_alarm_strategy:
            init_alarm_client(self.backend_api_url, self.backend_api_timeout)
            print(f"[ALARM STRATEGY] 报警策略系统已启用")
        else:
            print(f"[ALARM STRATEGY] 使用默认阈值: SOC={self.default_soc_threshold}%, TEMP={self.default_temp_threshold}°C")

        # 持续时间追踪器（用于DURATION触发时机）
        self.duration_tracker = {}  # {key: {start_time, value}}

        # 重连机制相关参数
        self.reconnect_attempts = 0  # 当前重连尝试次数
        self.max_reconnect_attempts = 20  # 最大重连尝试次数（0表示无限制）
        self.initial_reconnect_delay = 1.0  # 初始重连延迟（秒）
        self.max_reconnect_delay = 300.0  # 最大重连延迟（秒）
        self.reconnect_backoff_factor = 1.5  # 指数退避因子
        self.jitter_range = 0.3  # 随机抖动范围（±30%）
        self.should_reconnect = True  # 是否应该重连的标志
        self.reconnect_lock = threading.Lock()  # 重连锁，防止并发重连

        with self.engine.get_session() as session:
            # 根据船舶ID获取船舶信息
            self.ship_info = session.query(db.model.ShipInfoModel).filter(
                db.model.ShipInfoModel.id == self.args['ship_id']).first()

    def calculate_reconnect_delay(self):
        """
        计算重连延迟时间，使用指数退避和随机抖动

        Returns:
            float: 延迟时间（秒）

        指数退避公式：
        delay = min(initial_delay * (backoff_factor ^ attempt), max_delay)

        随机抖动公式：
        final_delay = delay * (1 + random.uniform(-jitter_range, jitter_range))
        """
        # 指数退避计算
        delay = min(
            self.initial_reconnect_delay * (self.reconnect_backoff_factor ** self.reconnect_attempts),
            self.max_reconnect_delay
        )

        # 添加随机抖动，避免多个客户端同时重连
        jitter = random.uniform(-self.jitter_range, self.jitter_range)
        final_delay = delay * (1 + jitter)

        # 确保延迟不为负数
        return max(0.1, final_delay)

    def reset_reconnect_state(self):
        """
        重置重连状态（连接成功后调用）
        """
        self.reconnect_attempts = 0

    def save_data(self):
        """
        将接收到的数据保存到数据库中
        """

        voyage_id = self.redis.get_str(f'{VOYAGE_KEY_PREFIX}{self.args["ship_id"]}')

        if len(self.tmp_msg_list) >= 2:  # 当接收到两条数据（bms数据和home数据）
            # 整合数据
            bms_data = self.tmp_msg_list[0]
            home_data = self.tmp_msg_list[1]
            data = {}
            for key in bms_data:
                data[key] = bms_data[key]
            for key in home_data:
                data[key] = home_data[key]

            # 如果位置缓存有效，使用缓存中的位置和航速数据替换WebSocket的数据
            if self.position_cache and self.position_cache.is_valid(max_age=10):
                cache_lat, cache_lon, cache_speed, cache_cog = self.position_cache.get_position_speed()
                data['lat'] = cache_lat
                data['lon'] = cache_lon
                data['ship_speed'] = cache_speed
                data['cog'] = cache_cog
                print(f"[CACHE] Using API position: lat={cache_lat}, lon={cache_lon}, speed={cache_speed}, cog={cache_cog}")
            else:
                print(f"[CACHE] Using WebSocket position (API cache invalid/missing)")

            # 在数据库中插入GPS日志数据
            with self.engine.get_session() as session:
                link_id = worker.get_id()
                gps_log = db.model.GPSLogModel()
                gps_log.id = worker.get_id()  # 使用雪花ID生成唯一ID
                gps_log.ship_id = self.args['ship_id']
                gps_log.longitude = data.get('lon', 0.0)  # 经度，优先使用API缓存
                gps_log.latitude = data.get('lat', 0.0)  # 纬度，优先使用API缓存
                gps_log.speed = data.get('ship_speed', 0.0)  # 船速，优先使用API缓存
                gps_log.direction = data.get('cog', 0.0)  # 航向，优先使用API缓存
                gps_log.link_id = link_id  # 链路ID
                gps_log.time = data.get('time_stamp', time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()))  # 时间戳，安全访问
                gps_log.create_date = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())  # 创建时间
                gps_log.update_date = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())  # 更新时间
                gps_log.voyage_id = voyage_id  # 航次ID

                # 插入左电池日志
                left_battery_log = db.model.BatteryLogModel()
                left_battery_log.id = worker.get_id()
                left_battery_log.ship_id = self.args['ship_id']
                left_battery_log.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                left_battery_log.soc = data.get('left_charge_soc', 100)  # 电池剩余电量，默认100%
                left_battery_log.voltage = data.get('left_bms_V', 0.0)  # 电池电压，默认0.0
                left_battery_log.electricity = data.get("left_bms_A", 0.0)  # 电池电流，默认0.0
                left_battery_log.power = data.get("left_bms_A", 0.0) * data.get("left_bms_V", 1.0)  # 功率，安全计算
                left_battery_log.temperature = data.get('left_bms_tmp', 25.0)  # 电池温度，默认25.0
                left_battery_log.position = 'l_1'  # 电池位置标识
                left_battery_log.link_id = link_id
                left_battery_log.voyageId = voyage_id

                # 插入右电池日志
                right_battery_log = db.model.BatteryLogModel()
                right_battery_log.id = worker.get_id()
                right_battery_log.ship_id = self.args['ship_id']
                right_battery_log.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                right_battery_log.soc = data.get('right_charge_soc', 100)  # 电池剩余电量，默认100%
                right_battery_log.voltage = data.get('right_bms_V', 0.0)  # 电池电压，默认0.0
                right_battery_log.electricity = data.get("right_bms_A", 0.0)  # 电池电流，默认0.0
                right_battery_log.power = data.get("right_bms_A", 0.0) * data.get("right_bms_V", 1.0)  # 功率，安全计算
                right_battery_log.temperature = data.get('right_bms_tmp', 25.0)  # 电池温度，默认25.0
                right_battery_log.position = 'r_1'  # 电池位置标识
                right_battery_log.link_id = link_id
                right_battery_log.voyageId = voyage_id

                # 插入左推进器数据
                left_propeller_data = db.model.PropellerDataModel()
                left_propeller_data.id = worker.get_id()
                left_propeller_data.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                left_propeller_data.rpm = data.get('rpm_left', 0)  # 左推进器转速，默认0
                left_propeller_data.degrees = data.get('deg_left', 0.0)  # 左推进器舵角，默认0.0
                left_propeller_data.power = data.get('output_left', 0.0)  # 左推进器功率，默认0.0
                left_propeller_data.position = 'left'  # 推进器位置
                left_propeller_data.ship_id = self.args['ship_id']
                left_propeller_data.status = 'RUNNING' if data.get('left_propeller_status', 0) == 1 else 'STOPPED'  # 左推进器状态
                left_propeller_data.link_id = link_id
                left_propeller_data.voyage_id = voyage_id

                # 插入右推进器数据
                right_propeller_data = db.model.PropellerDataModel()
                right_propeller_data.id = worker.get_id()
                right_propeller_data.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                right_propeller_data.rpm = data.get('rpm_right', 0)  # 右推进器转速，默认0
                right_propeller_data.degrees = data.get('deg_right', 0.0)  # 右推进器舵角，默认0.0
                right_propeller_data.power = data.get('output_right', 0.0)  # 右推进器功率，默认0.0
                right_propeller_data.position = 'right'  # 推进器位置
                right_propeller_data.ship_id = self.args['ship_id']
                right_propeller_data.status = 'RUNNING' if data.get('right_propeller_status', 0) == 1 else 'STOPPED'  # 右推进器状态
                right_propeller_data.link_id = link_id
                right_propeller_data.voyage_id = voyage_id

                # 调用用户注册的消息处理函数
                for handler in self.on_message_handler:
                    if callable(handler):
                        handler(ship_id=self.args['ship_id'],models={
                            'gps_log': gps_log,
                            'left_battery_log': left_battery_log,
                            'right_battery_log': right_battery_log,
                            'left_propeller_data': left_propeller_data,
                            'right_propeller_data': right_propeller_data
                        }, msgs_list=self.msg_list, ship_info=self.ship_info, db_session=session, redis_engine=self.redis)

                # 保存数据到数据库
                session.add(gps_log)
                session.add(left_battery_log)
                session.add(right_battery_log)
                session.add(left_propeller_data)
                session.add(right_propeller_data)
                session.commit()  # 提交事务
                session.refresh(gps_log)
                session.refresh(left_battery_log)
                session.refresh(right_battery_log)
                session.refresh(left_propeller_data)
                session.refresh(right_propeller_data)
                session.expunge(gps_log)
                session.expunge(left_battery_log)
                session.expunge(right_battery_log)
                session.expunge(left_propeller_data)
                session.expunge(right_propeller_data)
                message = {
                    'gps_log': gps_log.to_json(),
                    'left_battery_log': left_battery_log.to_json(),
                    'right_battery_log': right_battery_log.to_json(),
                    'left_propeller_data': left_propeller_data.to_json(),
                    'right_propeller_data': right_propeller_data.to_json()
                }
                # 将数据添加到消息列表中
                self.msg_list.append(message)
                self.redis.add_str(f'realtime:{self.args["ship_id"]}', json.dumps(message, cls=JSONEncoder))
                if len(self.msg_list) == WINDOW_SIZE:
                    # 当消息列表达到窗口大小时，只保留最新的10条数据
                    self.msg_list = self.msg_list[-WINDOW_SIZE:]
                self.tmp_msg_list.clear()  # 清空临时消息列表

                # 检测电池SOC并触发报警
                self.check_battery_alarm(left_battery_log, right_battery_log, gps_log.id)

    def check_battery_alarm(self, left_battery, right_battery, gps_log_id):
        """
        检测电池SOC和温度并在低于阈值时触发报警
        :param left_battery: 左电池日志对象
        :param right_battery: 右电池日志对象
        :param gps_log_id: GPS日志ID（关联数据）
        """
        try:
            if self.use_alarm_strategy:
                # 使用策略配置系统
                alarm_client = get_alarm_client()

                # 检查左电池SOC
                self.check_alarm_by_strategy(
                    alarm_client,
                    left_battery.soc,
                    'LOW_BATTERY',
                    gps_log_id,
                    {
                        'battery_position': '左电池' if left_battery.position == 'l_1' else '右电池',
                        'suggestion': '立即充电'
                    }
                )

                # 检查右电池SOC
                self.check_alarm_by_strategy(
                    alarm_client,
                    right_battery.soc,
                    'LOW_BATTERY',
                    gps_log_id,
                    {
                        'battery_position': '右电池' if right_battery.position == 'r_1' else '左电池',
                        'suggestion': '立即充电'
                    }
                )

                # 检查左电池温度
                self.check_alarm_by_strategy(
                    alarm_client,
                    left_battery.temperature,
                    'HIGH_TEMPERATURE',
                    gps_log_id,
                    {
                        'battery_position': '左电池' if left_battery.position == 'l_1' else '右电池',
                        'suggestion': '检查冷却系统'
                    }
                )

                # 检查右电池温度
                self.check_alarm_by_strategy(
                    alarm_client,
                    right_battery.temperature,
                    'HIGH_TEMPERATURE',
                    gps_log_id,
                    {
                        'battery_position': '右电池' if right_battery.position == 'r_1' else '左电池',
                        'suggestion': '检查冷却系统'
                    }
                )
            else:
                # 使用默认阈值（向后兼容）
                # 检查左电池SOC
                if left_battery.soc < self.default_soc_threshold:
                    self.create_soc_alarm(left_battery.soc, left_battery.position, gps_log_id)

                # 检查右电池SOC
                if right_battery.soc < self.default_soc_threshold:
                    self.create_soc_alarm(right_battery.soc, right_battery.position, gps_log_id)

                # 检查温度
                temp_threshold = self.default_temp_threshold
                if left_battery.temperature > temp_threshold:
                    print(f"[ALARM] ⚠ Left battery temperature high: {left_battery.temperature:.1f}°C")
                if right_battery.temperature > temp_threshold:
                    print(f"[ALARM] ⚠ Right battery temperature high: {right_battery.temperature:.1f}°C")

        except Exception as e:
            print(f"[ALARM] Battery alarm check failed: {e}")

    def check_alarm_by_strategy(self, alarm_client, value, alarm_type, gps_log_id, template_variables=None):
        """
        根据策略检查并触发报警

        :param alarm_client: 报警客户端
        :param value: 当前值
        :param alarm_type: 报警类型
        :param gps_log_id: GPS日志ID（关联数据）
        :param template_variables: 模板变量（如battery_position, suggestion等）
        """
        try:
            # 获取策略
            strategy = alarm_client.get_effective_strategy(self.args['ship_id'], alarm_type)

            # 没有配置策略，跳过
            if not strategy:
                return

            # 检查触发条件
            if not alarm_client.check_trigger_condition(value, strategy):
                return

            # 获取触发时机
            trigger_timing = strategy.get('triggerTiming', {})
            timing_code = trigger_timing.get('code') if isinstance(trigger_timing, dict) else trigger_timing

            if timing_code == 'immediate':
                # 立即触发
                self.trigger_alarm(alarm_client, alarm_type, value, gps_log_id, template_variables)
            elif timing_code == 'duration':
                # 持续时间触发
                self.check_duration_trigger(alarm_client, strategy, alarm_type, value, gps_log_id, template_variables)

        except Exception as e:
            print(f"[ALARM STRATEGY] Check alarm failed: {e}")

    def check_duration_trigger(self, alarm_client, strategy, alarm_type, value, gps_log_id, template_variables):
        """
        检查持续时间触发

        :param alarm_client: 报警客户端
        :param strategy: 策略对象
        :param alarm_type: 报警类型
        :param value: 当前值
        :param gps_log_id: GPS日志ID
        :param template_variables: 模板变量
        """
        try:
            duration_seconds = strategy.get('durationSeconds', 0)
            if duration_seconds <= 0:
                return

            # 创建追踪器key
            tracker_key = f"{self.args['ship_id']}_{alarm_type}_{strategy.get('id')}"

            # 检查是否正在追踪
            if tracker_key in self.duration_tracker:
                tracker = self.duration_tracker[tracker_key]
                elapsed_time = time.time() - tracker['start_time']

                # 检查是否满足持续时间
                if elapsed_time >= duration_seconds:
                    # 触发报警
                    self.trigger_alarm(alarm_client, alarm_type, value, gps_log_id, template_variables)
                    # 清除追踪器
                    del self.duration_tracker[tracker_key]
                    print(f"[ALARM STRATEGY] Duration trigger: {alarm_type}, elapsed={elapsed_time:.1f}s")
                else:
                    # 更新追踪器的值（保持触发状态）
                    tracker['value'] = value
            else:
                # 开始追踪
                self.duration_tracker[tracker_key] = {
                    'start_time': time.time(),
                    'value': value
                }
                print(f"[ALARM STRATEGY] Start tracking: {alarm_type}, duration={duration_seconds}s")

        except Exception as e:
            print(f"[ALARM STRATEGY] Duration trigger check failed: {e}")

    def trigger_alarm(self, alarm_client, alarm_type, value, gps_log_id, template_variables=None):
        """
        触发报警

        :param alarm_client: 报警客户端
        :param alarm_type: 报警类型
        :param value: 当前值
        :param gps_log_id: GPS日志ID
        :param template_variables: 模板变量
        """
        try:
            # 检查是否最近已经报过警（避免频繁报警）
            current_time = time.time()
            alarm_key = f"{self.args['ship_id']}_{alarm_type}"

            # 对于不同的电池位置，使用不同的key
            if template_variables and 'battery_position' in template_variables:
                alarm_key = f"{alarm_key}_{template_variables['battery_position']}"

            last_alarm_time = self.last_alarm_time.get(alarm_key, 0)

            if current_time - last_alarm_time < 600:  # 10分钟 = 600秒
                print(f"[ALARM STRATEGY] Skipped: Recent alarm for {alarm_key}, value={value:.2f}")
                return

            # 调用报警客户端创建报警
            success = alarm_client.apply_strategy(
                ship_id=self.args['ship_id'],
                alarm_type=alarm_type,
                value=value,
                related_data_id=gps_log_id,
                template_variables=template_variables or {}
            )

            if success:
                print(f"[ALARM STRATEGY] ✓ Alarm created: {alarm_type}, value={value:.2f}")
                # 更新最后报警时间
                self.last_alarm_time[alarm_key] = current_time
            else:
                print(f"[ALARM STRATEGY] ✗ Failed to create alarm: {alarm_type}")

        except Exception as e:
            print(f"[ALARM STRATEGY] Trigger alarm failed: {e}")

    def create_soc_alarm(self, soc_value, battery_position, gps_log_id):
        """
        创建低电量报警（向后兼容方法）

        :param soc_value: 当前SOC值
        :param battery_position: 电池位置（l_1或r_1）
        :param gps_log_id: 关联的GPS日志ID
        """
        try:
            # 检查是否在最近10分钟内已经报过警（避免频繁报警）
            current_time = time.time()
            alarm_key = f"{self.args['ship_id']}_{battery_position}"
            last_alarm_time = self.last_alarm_time.get(alarm_key, 0)

            if current_time - last_alarm_time < 600:  # 10分钟 = 600秒
                print(f"[ALARM] Skipped: Recent alarm for {battery_position}, SOC={soc_value:.1f}%")
                return

            # 如果启用了策略系统，使用策略客户端
            if self.use_alarm_strategy:
                alarm_client = get_alarm_client()
                template_vars = {
                    'battery_position': '左电池' if battery_position == 'l_1' else '右电池',
                    'suggestion': '立即充电'
                }
                self.trigger_alarm(alarm_client, 'LOW_BATTERY', soc_value, gps_log_id, template_vars)
            else:
                # 使用旧的API方式（向后兼容）
                api_url = f"{self.backend_api_url}/api/alarm/low-soc"
                payload = {
                    'shipId': self.args['ship_id'],
                    'socValue': soc_value,
                    'relatedDataId': gps_log_id
                }

                headers = {
                    'Content-Type': 'application/json'
                }

                response = requests.post(
                    api_url,
                    json=payload,
                    headers=headers,
                    timeout=self.backend_api_timeout
                )

                if response.status_code == 200:
                    print(f"[ALARM] ✓ Low battery alarm created: {battery_position}, SOC={soc_value:.1f}%")
                    # 更新最后报警时间
                    self.last_alarm_time[alarm_key] = current_time
                else:
                    print(f"[ALARM] ✗ Failed to create alarm: HTTP {response.status_code}")

        except requests.exceptions.Timeout:
            print(f"[ALARM] ✗ Request timeout: Backend API not responding")
        except requests.exceptions.ConnectionError:
            print(f"[ALARM] ✗ Connection error: Cannot connect to backend API at {self.backend_api_url}")
        except Exception as e:
            print(f"[ALARM] ✗ Error creating alarm: {e}")

    def on_message(self, _ws, message):
        """
        WebSocket消息接收处理函数
        :param _ws: WebSocket实例
        :param message: 收到的消息
        """
        try:
            msg = json.loads(message)  # 解析JSON格式的消息
            if msg['type'] == 'heartbeat':
                return  # 如果是心跳消息，忽略
            flag = msg['flag']  # 消息标识
            data = msg['data']  # 消息数据

            # 安全获取嵌套字典值的辅助函数
            def safe_get(data_dict, *keys, default=None):
                """安全获取嵌套字典中的值，如果任何key不存在则返回默认值"""
                try:
                    result = data_dict
                    for key in keys:
                        result = result[key]
                    return result
                except (KeyError, TypeError):
                    return default

            if flag == 'bms':
                # 解析电池管理系统数据 - 添加安全的key访问
                umbilical_data = safe_get(data, 'data', 'data', 'MainBatterySystems/Umbilical', default={})

                # 获取电压数据，如果不存在则使用默认值
                left_bms_V = safe_get(umbilical_data, 'VoltageLeft_V_1', default=0.0)
                right_bms_V = safe_get(umbilical_data, 'VoltageRight_V_1', default=0.0)

                # 获取电流数据，如果不存在则使用默认值
                left_bms_A = safe_get(umbilical_data, 'CurrentLeft_A_1', default=0.0)
                right_bms_A = safe_get(umbilical_data, 'CurrentRight_A_1', default=0.0)

                # 计算左电池温度
                try:
                    left_bms_tmp = safe_get(umbilical_data, 'EnvLeft_A_1', default=None)
                    if left_bms_tmp is None:
                        # 如果没有直接的温度数据，从电池簇数据计算平均值
                        temps = safe_get(data, 'data', 'data', 'MainBatterySystems/RechargeableBatteryCluster_Left', default={})
                        temp_values = []
                        for temp_key in temps:
                            if 'EnvTemperature' in temp_key:
                                temp_values.append(temps[temp_key])
                        left_bms_tmp = sum(temp_values) / len(temp_values) if temp_values else 25.0
                except (KeyError, TypeError, ZeroDivisionError):
                    left_bms_tmp = 25.0  # 默认温度

                # 计算右电池温度
                try:
                    right_bms_tmp = safe_get(umbilical_data, 'EnvRight_A_1', default=None)
                    if right_bms_tmp is None:
                        # 如果没有直接的温度数据，从电池簇数据计算平均值
                        temps = safe_get(data, 'data', 'data', 'MainBatterySystems/RechargeableBatteryCluster_Right', default={})
                        temp_values = []
                        for temp_key in temps:
                            if 'EnvTemperature' in temp_key:
                                temp_values.append(temps[temp_key])
                        right_bms_tmp = sum(temp_values) / len(temp_values) if temp_values else 25.0
                except (KeyError, TypeError, ZeroDivisionError):
                    right_bms_tmp = 25.0  # 默认温度

                # BMS格式：只获取电压、电流、温度数据，不获取SOC（SOC从home格式获取）
                # 将电池管理系统数据添加到临时消息列表
                self.tmp_msg_list.append({
                    'left_bms_V': left_bms_V,
                    'right_bms_V': right_bms_V,
                    'left_bms_A': left_bms_A,
                    'right_bms_A': right_bms_A,
                    'left_bms_tmp': left_bms_tmp,
                    'right_bms_tmp': right_bms_tmp,
                })

            elif flag == 'home':
                # 解析船舶状态和位置数据 - 添加安全的key访问
                sail_status = safe_get(data, 'sail_status', default=0)
                is_online = safe_get(data, 'is_online', default=0)
                ship_name = safe_get(data, 'ship_name', default='Unknown')

                # 处理时间戳
                data_time = safe_get(data, 'data', 'time', default=0)
                timestamp = float(data_time / 1000) if data_time else time.time()
                time_array = time.localtime(timestamp)
                time_stamp = time.strftime("%Y-%m-%d %H:%M:%S", time_array)

                # 安全获取AIS数据
                ais_data = safe_get(data, 'data', 'data', 'OtherSystem/AIS', default={})
                lat = safe_get(ais_data, 'LatGD_S_1', default=0.0)
                lon = safe_get(ais_data, 'LonGD_S_1', default=0.0)
                cog = safe_get(ais_data, 'CourseOverGround_S_1', default=0.0)
                ship_speed = safe_get(ais_data, 'ShipSpeed_S_1', default=0.0)

                # 安全获取推进器状态
                left_converter = safe_get(data, 'data', 'data', 'ElectricPowerSystem/Frequency_Converter_Left', default={})
                left_propeller_status = safe_get(left_converter, 'RunningStatus_B_1', default=0)

                right_converter = safe_get(data, 'data', 'data', 'ElectricPowerSystem/Frequency_Converter_Right', default={})
                right_propeller_status = safe_get(right_converter, 'RunningStatus_B_1', default=0)

                # 从Umbilical获取左右电仓SOC数据
                umbilical_data = safe_get(data, 'data', 'data', 'MainBatterySystems/Umbilical', default={})

                # 获取左电仓SOC：data.data.data["MainBatterySystems/Umbilical"]["SOCLeft_PCT_1"]
                left_charge_soc = safe_get(umbilical_data, 'SOCLeft_PCT_1', default=100.0)

                # 获取右电仓SOC：data.data.data["MainBatterySystems/Umbilical"]["SOCRight_PCT_1"]
                right_charge_soc = safe_get(umbilical_data, 'SOCRight_PCT_1', default=100.0)

                # 调试信息：打印SOC数据获取情况
                print(f"[HOME SOC] Left: {left_charge_soc}, Right: {right_charge_soc}, Umbilical keys: {list(umbilical_data.keys())[:5] if umbilical_data else 'No data'}")

                # 安全获取左推进器数据
                output_left = safe_get(left_converter, 'OutputAvailablePower_kW_1',
                               safe_get(left_converter, 'OutputAvailablePower_A_1', default=0.0))
                rpm_left = safe_get(left_converter, 'RotatingSpeed_RPM_1', default=0)

                # 安全获取舵角数据
                left_pod = safe_get(data, 'data', 'data', 'OtherSystem/Pod_Left', default={})
                deg_left = safe_get(left_pod, 'RudderAngle_Deg_1', default=0.0)

                right_pod = safe_get(data, 'data', 'data', 'OtherSystem/Pod_Right', default={})
                deg_right = safe_get(right_pod, 'RudderAngle_Deg_1', default=0.0)

                # 安全获取右推进器数据
                output_right = safe_get(right_converter, 'OutputAvailablePower_kW_1',
                                safe_get(right_converter, 'OutputAvailablePower_A_1', default=0.0))
                rpm_right = safe_get(right_converter, 'RotatingSpeed_RPM_1', default=0)

                # 将船舶数据添加到临时消息列表
                self.tmp_msg_list.append({
                    'sail_status': sail_status,
                    'is_online': is_online,
                    'ship_name': ship_name,
                    'time_stamp': time_stamp,
                    'lat': lat,
                    'lon': lon,
                    'cog': cog,
                    'ship_speed': ship_speed,
                    'left_charge_soc': left_charge_soc,
                    'right_charge_soc': right_charge_soc,
                    'output_left': output_left,
                    'rpm_left': rpm_left,
                    'output_right': output_right,
                    'rpm_right': rpm_right,
                    'deg_left': deg_left,
                    'deg_right': deg_right,
                    'left_propeller_status': left_propeller_status,
                    'right_propeller_status': right_propeller_status
                })

            try:
                self.save_data()  # 尝试保存数据
            except Exception as e:
                print(f"Save data error: {e}")  # 记录错误但不中断
                self.tmp_msg_list.clear()
            # 发送心跳消息，维持连接
            try:
                self.ws.send(json.dumps({
                    "type": "heartbeat"
                }))
            except Exception as e:
                print(f"Heartbeat send error: {e}")

        except Exception as e:
            print(f"Message processing error: {e}")  # 改进错误信息

    def on_error(self, _ws, error):
        """
        WebSocket连接发生错误时的回调函数
        """
        print("\033[31m### error ###")
        self.tmp_msg_list.clear()
        print(error)
        print("### ===error=== ###\033[m")

    def on_close(self, _ws, close_status_code, close_msg):
        """
        WebSocket连接关闭时的回调函数
        使用指数退避和随机抖动进行重连
        """
        print("\033[31m### closed ###")
        print(f"status_code: {close_status_code}, msg: {close_msg}")
        print("### ===closed=== ###\033[m")

        self.tmp_msg_list.clear()

    def on_open(self, ws):
        """
        WebSocket连接建立时的回调函数
        """
        print("\033[32m### opened ###\033[m")
        self.tmp_msg_list.clear()

        # 连接成功，重置重连状态
        self.reset_reconnect_state()
        # print(f"[RECONNECT] Connection established! Reset reconnection attempts.")


        # 发送开始接收数据的请求
        ws.send(json.dumps({
            "type": "realtime",
            "beginTime": "",
            "endTime": "",
            "interval": self.args['interval'],
            "flag": "bms"
        }))

    def start_ws(self):
        """
        启动WebSocket连接
        """
        self.tmp_msg_list.clear()  # 清空临时消息列表
        self.msg_list.clear()  # 清空消息列表
        self.ws = WebSocketApp(
            url=self.gt2_url,  # WebSocket的连接URL
            on_open=self.on_open,  # 连接成功回调
            on_message=self.on_message,  # 接收到消息回调
            on_error=self.on_error,  # 错误回调
            on_close=self.on_close,  # 关闭回调
            header={  # 请求头
                'Pragma': 'no-cache',
                'accept-encoding': 'gzip, deflate, br, zstd',
                'Accept-Language': 'zh-CN,zh;q=0.9',
                'User-Agent': "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                              "Chrome/129.0.0.0 Safari/537.36",
                'Upgrade': 'websocket',
                'Cache-Control': 'no-cache',
                'Connection': 'Upgrade',
            },
        )
        # 启动WebSocket连接

        while self.max_reconnect_attempts >= self.reconnect_attempts:
            try:
                self.ws.run_forever(origin='https://monitor.epops.purvar.com')
            except Exception:
                if not self.should_reconnect:
                    break
                self.reconnect_attempts += 1
                delay = self.calculate_reconnect_delay()
                asyncio.sleep(delay)


    def stop(self):
        """
        停止WebSocket连接并禁用重连
        """
        print("[STOP] Stopping WebSocket connection...")
        self.should_reconnect = False
        if self.ws:
            self.ws.close()

    def add_handler(self, handler):
        """
        注册消息处理函数
        :param handler: 消息处理函数
        """
        if not callable(handler):
            raise ValueError("handler must be a callable object")
        self.on_message_handler.append(handler)

    def remove_handler(self, handler):
        """
        移除消息处理函数
        :param handler: 消息处理函数
        """
        if handler in self.on_message_handler:
            self.on_message_handler.remove(handler)

    def get_reconnect_stats(self):
        """
        获取当前重连统计信息

        Returns:
            dict: 包含重连相关统计信息的字典
        """
        next_delay = self.calculate_reconnect_delay()
        return {
            'reconnect_attempts': self.reconnect_attempts,
            'max_reconnect_attempts': self.max_reconnect_attempts if self.max_reconnect_attempts > 0 else 'unlimited',
            'next_reconnect_delay': next_delay,
            'should_reconnect': self.should_reconnect
        }
