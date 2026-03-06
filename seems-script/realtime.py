#!/usr/bin/env python3
# _*_ coding:utf-8 _*_
import json
import time

from websocket import WebSocketApp
from handler.voyage_handler import REDIS_KEY_PREFIX as VOYAGE_KEY_PREFIX
import db
from db.sql.snowflakeId import IdWorker
from encoder import JSONEncoder

WINDOW_SIZE = 10  # 设置数据缓冲窗口大小，最多保存最新的10条数据

worker = IdWorker(1, 2, 0)  # 使用雪花算法生成唯一ID，worker为ID生成器实例


class DataWebSocket:
    def __init__(self, args):
        """
        WebSocket 数据处理类的初始化方法
        :param args: 输入的参数（例如ship_id、interval等）
        """
        self.args = args
        self.gt2_url = 'wss://server.monitor.epops.purvar.com/api/ws/real?ship_id=yungang_diantuo2'  # WebSocket的URL
        self.ws = None  # WebSocket实例
        self.tmp_msg_list = []  # 临时消息列表，用于存储接收到的消息
        self.msg_list = []  # 消息列表，用于存储处理过的消息
        self.last_time = time.time()  # 记录上一次操作的时间戳
        self.engine = db.SQLEngine(self.args['mysql']['host'], self.args['mysql']['port'], self.args['mysql']['user'], self.args['mysql']['password'], self.args['mysql']['database'])  # 数据库连接
        self.redis = db.RedisEngine(self.args['redis']['host'], self.args['redis']['port'], self.args['redis']['db'],self.args['redis']['password'])  # Redis连接
        self.on_message_handler = []  # 存储用户注册的消息处理函数
        with self.engine.get_session() as session:
            # 根据船舶ID获取船舶信息
            self.ship_info = session.query(db.model.ShipInfoModel).filter(
                db.model.ShipInfoModel.id == self.args['ship_id']).first()

    def save_data(self):
        """
        将接收到的数据保存到数据库中
        """

        voyage_id = self.redis.get_str(f'{VOYAGE_KEY_PREFIX}{self.args["ship_id"]}')

        if len(self.tmp_msg_list) == 2:  # 当接收到两条数据（bms数据和home数据）
            # 整合数据
            bms_data = self.tmp_msg_list[0]
            home_data = self.tmp_msg_list[1]
            data = {}
            for key in bms_data:
                data[key] = bms_data[key]
            for key in home_data:
                data[key] = home_data[key]

            # 在数据库中插入GPS日志数据
            with self.engine.get_session() as session:
                link_id = worker.get_id()
                gps_log = db.model.GPSLogModel()
                gps_log.id = worker.get_id()  # 使用雪花ID生成唯一ID
                gps_log.ship_id = self.args['ship_id']
                gps_log.longitude = data['lon']  # 经度
                gps_log.latitude = data['lat']  # 纬度
                gps_log.speed = data['ship_speed']  # 船速
                gps_log.direction = data['cog']  # 航向
                gps_log.link_id = link_id  # 链路ID
                gps_log.time = data['time_stamp']  # 时间戳
                gps_log.create_date = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())  # 创建时间
                gps_log.update_date = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())  # 更新时间
                gps_log.voyage_id = voyage_id  # 航次ID

                # 插入左电池日志
                left_battery_log = db.model.BatteryLogModel()
                left_battery_log.id = worker.get_id()
                left_battery_log.ship_id = self.args['ship_id']
                left_battery_log.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                left_battery_log.soc = data['left_charge_soc']  # 电池剩余电量
                left_battery_log.voltage = data['left_bms_V']  # 电池电压
                left_battery_log.electricity = data["left_bms_A"]  # 电池电流
                left_battery_log.power = data["left_bms_A"] * data["left_bms_V"]  # 功率
                left_battery_log.temperature = data['left_bms_tmp']  # 电池温度
                left_battery_log.position = 'l_1'  # 电池位置标识
                left_battery_log.link_id = link_id
                left_battery_log.voyageId = voyage_id

                # 插入右电池日志
                right_battery_log = db.model.BatteryLogModel()
                right_battery_log.id = worker.get_id()
                right_battery_log.ship_id = self.args['ship_id']
                right_battery_log.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                right_battery_log.soc = data['right_charge_soc']  # 电池剩余电量
                right_battery_log.voltage = data['right_bms_V']  # 电池电压
                right_battery_log.electricity = data["right_bms_A"]  # 电池电流
                right_battery_log.power = data["right_bms_A"] * data["right_bms_V"]  # 功率
                right_battery_log.temperature = data['right_bms_tmp']  # 电池温度
                right_battery_log.position = 'r_1'  # 电池位置标识
                right_battery_log.link_id = link_id
                right_battery_log.voyageId = voyage_id

                # 插入左推进器数据
                left_propeller_data = db.model.PropellerDataModel()
                left_propeller_data.id = worker.get_id()
                left_propeller_data.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                left_propeller_data.rpm = data['rpm_left']  # 左推进器转速
                left_propeller_data.degrees = data['deg_left']  # 左推进器舵角
                left_propeller_data.power = data['output_left']  # 左推进器功率
                left_propeller_data.position = 'left'  # 推进器位置
                left_propeller_data.ship_id = self.args['ship_id']
                left_propeller_data.status = 'RUNNING' if data['left_propeller_status'] == 1 else 'STOPPED'  # 左推进器状态
                left_propeller_data.link_id = link_id
                left_propeller_data.voyage_id = voyage_id

                # 插入右推进器数据
                right_propeller_data = db.model.PropellerDataModel()
                right_propeller_data.id = worker.get_id()
                right_propeller_data.time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                right_propeller_data.rpm = data['rpm_right']  # 右推进器转速
                right_propeller_data.degrees = data['deg_right']  # 右推进器舵角
                right_propeller_data.power = data['output_right']  # 右推进器功率
                right_propeller_data.position = 'right'  # 推进器位置
                right_propeller_data.ship_id = self.args['ship_id']
                right_propeller_data.status = 'RUNNING' if data['left_propeller_status'] == 1 else 'STOPPED'  # 右推进器状态
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
            if flag == 'bms':
                # 解析电池管理系统数据
                left_bms_V = data['data']['data']['MainBatterySystems/Umbilical']['VoltageLeft_V_1']
                right_bms_V = data['data']['data']['MainBatterySystems/Umbilical']['VoltageRight_V_1']
                left_bms_A = data['data']['data']['MainBatterySystems/Umbilical']['CurrentLeft_A_1']
                right_bms_A = data['data']['data']['MainBatterySystems/Umbilical']['CurrentRight_A_1']
                try:
                    left_bms_tmp = data['data']['data']['MainBatterySystems/Umbilical']['EnvLeft_A_1']
                except KeyError:
                    left_bms_tmp = 0.0
                    temps = data['data']['data']['MainBatterySystems/RechargeableBatteryCluster_Left']
                    for temp in temps:
                        if 'EnvTemperature' in temp:
                            left_bms_tmp += temps[temp]
                    left_bms_tmp = left_bms_tmp / 16.0
                try:
                    right_bms_tmp = data['data']['data']['MainBatterySystems/Umbilical']['EnvRight_A_1']
                except KeyError:
                    right_bms_tmp = 0.0
                    temps = data["data"]["data"]["MainBatterySystems/RechargeableBatteryCluster_Right"]
                    for temp in temps:
                        if 'EnvTemperature' in temp:
                            right_bms_tmp += temps[temp]
                    right_bms_tmp = right_bms_tmp / 16.0

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
                # 解析船舶状态和位置数据
                sail_status = data['sail_status']
                is_online = data['is_online']
                ship_name = data['ship_name']
                timestamp = float(data['data']['time'] / 1000)
                time_array = time.localtime(timestamp)
                time_stamp = time.strftime("%Y-%m-%d %H:%M:%S", time_array)
                lat = data['data']['data']['OtherSystem/AIS']['LatGD_S_1']
                lon = data['data']['data']['OtherSystem/AIS']['LonGD_S_1']
                cog = data['data']['data']['OtherSystem/AIS']['CourseOverGround_S_1']
                ship_speed = data['data']['data']['OtherSystem/AIS']['ShipSpeed_S_1']
                left_propeller_status = data['data']['data']["ElectricPowerSystem/Frequency_Converter_Left"][
                    'RunningStatus_B_1']
                right_propeller_status = data['data']['data']["ElectricPowerSystem/Frequency_Converter_Right"][
                    'RunningStatus_B_1']

                # 解析电池剩余电量和电机功率等数据
                try:
                    left_charge_soc = data["data"]["data"]["MainBatterySystems/Umbilical"]["SOCLeft_P_1"]
                except KeyError:
                    left_charge_soc = data["data"]["data"]["MainBatterySystems/Umbilical"]["SOCLeft_PCT_1"]
                try:
                    right_charge_soc = data["data"]["data"]["MainBatterySystems/Umbilical"]["SOCRight_P_1"]
                except KeyError:
                    right_charge_soc = data["data"]["data"]["MainBatterySystems/Umbilical"]["SOCRight_PCT_1"]

                try:
                    output_left = data["data"]["data"]["ElectricPowerSystem/Frequency_Converter_Left"][
                        "OutputAvailablePower_A_1"]
                except KeyError:
                    output_left = data["data"]["data"]["ElectricPowerSystem/Frequency_Converter_Left"][
                        "OutputAvailablePower_kW_1"]
                rpm_left = data["data"]["data"]["ElectricPowerSystem/Frequency_Converter_Left"]["RotatingSpeed_RPM_1"]
                deg_left = data["data"]["data"]["OtherSystem/Pod_Left"]["RudderAngle_Deg_1"]
                deg_right = data["data"]["data"]["OtherSystem/Pod_Right"]["RudderAngle_Deg_1"]
                try:
                    output_right = data["data"]["data"]["ElectricPowerSystem/Frequency_Converter_Right"][
                        "OutputAvailablePower_A_1"]
                except KeyError:
                    output_right = data["data"]["data"]["ElectricPowerSystem/Frequency_Converter_Right"][
                        "OutputAvailablePower_kW_1"]
                rpm_right = data["data"]["data"]["ElectricPowerSystem/Frequency_Converter_Right"]["RotatingSpeed_RPM_1"]

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
                raise e  # 如果发生异常，抛出异常
            # 发送心跳消息，维持连接
            self.ws.send(json.dumps({
                "type": "heartbeat"
            }))

        except Exception as e:
            print(f"Error: {e}")  # 打印异常信息

    def on_error(self, _ws, error):
        """
        WebSocket连接发生错误时的回调函数
        """
        print("\033[31m### error ###")
        print(error)

        print("### ===error=== ###\033[m")

    def on_close(self, _ws, close_status_code, close_msg):
        """
        WebSocket连接关闭时的回调函数
        """
        print("\033[31m### closed ###")
        print(f"status_code: {close_status_code}, msg: {close_msg}")
        print("### ===closed=== ###\033[m")
        print("### 重新连接 ###")
        self.start_ws()  # 重新连接WebSocket

    def on_open(self, ws):
        """
        WebSocket连接建立时的回调函数
        """
        print("\033[32m### opened ###\033[m")
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
        self.ws.run_forever(origin='https://monitor.epops.purvar.com')

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
