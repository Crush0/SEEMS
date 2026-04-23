#!/usr/bin/env python3
# _*_ coding:utf-8 _*_
"""
船舶位置API调用类 - 基于shipxy.com第三方接口
"""
import hashlib
import time
import requests
from datetime import datetime, timezone
import json
from apscheduler.schedulers.background import BackgroundScheduler
from cache import ShipDataCache


def get_salt():
    """
    对应 JS 中的 j(h()) 逻辑
    """
    f = [
        "cc203d8f43c9571942a6da8078f0db41", "a47c89688ce270f630738bc71f8b83e5",
        "6adfe523c5dd62ebad515a73d70ef8de", "5a566f1910d346b2f69d495847122106",
        "a2edd2cc83fb5aa97cdbcfc2c0505ef4", "d81c0220c1b3ed77e0ced8fb8c843f59",
        "dd17e6b62f395fc590daa7a0da155b0e", "b9406bd442489c33b7b9d430479163cd",
        "cd5453f9d9eba716b394d8af4e1042d0", "9ee7b5a006f5385c141d546f2cd33886"
    ]
    # 获取 UTC 日期
    utc_day = datetime.now(timezone.utc).day
    # 根据日期取模
    raw_str = f[utc_day % len(f)]

    # 剔除索引为 11, 13, 15, 17, 19 的字符
    exclude_indices = {11, 13, 15, 17, 19}
    salt = "".join([char for i, char in enumerate(raw_str) if i not in exclude_indices])
    return salt


def format_query_string(data):
    """
    对应 JS 中的 i(a) 逻辑
    """
    if not data:
        return ""

    if isinstance(data, list):
        return "jsonArray=" + json.dumps(list(data), separators=(',', ':'))

    # 按 Key 忽略大小写排序
    keys = sorted(data.keys(), key=lambda x: x.lower())

    parts = []
    for k in keys:
        v = data[k]
        if isinstance(v, (list, dict)):
            # 序列化整个数据对象
            val_str = json.dumps(data, separators=(',', ':'))
            parts.append(f"{k}={val_str}")
        else:
            parts.append(f"{k}={v}")

    return "&".join(parts)


def sign(data):
    """
    主加密函数
    """
    # 1. 处理数据字符串
    query_str = format_query_string(data)

    # 2. 获取盐值
    salt = get_salt()

    # 3. 获取当前时间戳 (秒)
    timestamp = int(time.time())

    # 4. 拼接字符串: 数据串(如果有)+& + t=时间戳 + 盐值
    if query_str:
        sign_content = f"{query_str}&t={timestamp}{salt}"
    else:
        sign_content = f"t={timestamp}{salt}"

    # 5. 计算 MD5
    md5_hash = hashlib.md5(sign_content.encode('utf-8')).hexdigest()

    return {
        "sign": md5_hash,
        "timestamp": str(timestamp)
    }


class ShipPositionAPI:
    """船舶位置API调用类 - shipxy.com第三方接口"""

    def __init__(self, mmsi, interval=5):
        """
        初始化API调用类
        :param mmsi: 船舶MMSI编号
        :param interval: 请求间隔（秒）
        """
        self.mmsi = str(mmsi)
        self.interval = interval
        self.cache = ShipDataCache()
        self.scheduler = None

        # API配置
        self.api_url = 'https://www.shipxy.com/ship/GetShipm'

        # Cookies配置（根据实际情况配置）
        self.cookies = {
            '.UserAuth2': '243F751ED9D2EE33CA753C1F86F1B9BEEF482D14A12874FC5D63AF5D2AA9FCBA378BD6EDCB4C017001F84A7E75A5C5FAE5CE9E0381B2E5FB6C39D8F9899E82B57C3202CBE26B56067C60EFCC349CCB7C01DB8726B274C9222733D7D2D3A6AE1325189AF46B7081EA96D50E2B67F59A4F99E9004CCE005088C434D7ED0A0C516362DDBF3171B27D6E608037AACFA50390DB0F90E73EAF640E35E31C3BC67BCA3285D3658F',
            '.UserAuth3': 'k8D4c1jZzUr9t9SK2MwcxA==',
            'UserAuthUCenter': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtfS1JMRk9TM1dPYk9ZcUN2ZEpKS2I3ZGo1TSIsImtpZCI6ImtfS1JMRk9TM1dPYk9ZcUN2ZEpKS2I3ZGo1TSJ9.eyJpc3MiOiJodHRwczovL2lkMi5lbGFuZS5jb20uY24vY29yZSIsImF1ZCI6Imh0dHBzOi8vaWQyLmVsYW5lLmNvbS5jbi9jb3JlL3Jlc291cmNlcyIsImV4cCI6MTc4MDU0NDIyMSwibmJmIjoxNzcyNzY4MjIxLCJjbGllbnRfaWQiOiJIWVFfUmVzX0FsbEluIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInVzZXJhcGkiXSwic3ViIjoiOTJhM2I0OTU0OGU2MjkwNyIsImF1dGhfdGltZSI6MTc3Mjc2ODIyMSwiaWRwIjoiaWRzcnYiLCJhbXIiOlsicGFzc3dvcmQiXX0.C4YT3PhZeXKEwdCByWigNkEaIjiRCl7JX9pvuxwOksaikUcFeETV4CAVpEeVfG39qLfW_Q6TeUx9rO5ZdUSHWPTRay_GKxLNk-Cx9spPCeFHiGHWpFUe1oeuwX-F6vTQOV8AAlJSFsLICyATQ6ffwVRhCwhl7ahi6UAnldlClJDjH0CHJaM7Cqjz2HKceQhT9-jFdrOl2tVD1ovGob4y2hO_UTkcfj0oGC56CFj8ZoIVuu5Zk0NX_PpDaNdDwNWfe22Wr_aSpbx26ubBAPQKe-HZkhz0gi6TcF8esmGW1ZrCi3t6L3hb9a_vhM6bPk0qApsUloonASmRE5CAmOSr_w',
            'FD857C2AF68165D4': 'zyyrA+PlinzeujmR5aoqsUgSbz/qKGOWtH1Q6NdaD0v4CAqDOFAuOXHC4c2qndpo',
            '_elane_rastar_tri': '1',
            'tc_TC': '',
            '_elane_shipfilter_type': '%u8D27%u8239%2C%u96C6%u88C5%u7BB1%u8239%2C%u6CB9%u8F6E%2C%u5F15%u822A%u8239%2C%u62D6%u8F6E%2C%u62D6%u5F15%2C%u6E14%u8239%2C%u6355%u635E%2C%u5BA2%u8239%2C%u641C%u6551%u8239%2C%u6E2F%u53E3%u4F9B%u5E94%u8239%2C%u88C5%u6709%u9632%u6C61%u88C5%u7F6E%u548C%u8BBE%u5907%u7684%u8239%u8236%2C%u6267%u6CD5%u8247%2C%u5907%u7528-%u7528%u4E8E%u5F53%u5730%u8239%u8236%u7684%u4EFB%u52A1%u5206%u914D%2C%u5907%u7528-%u7528%u4E8E%u5F53%u5730%u8239%u8236%u7684%u4EFB%u52A1%u5206%u914D%2C%u533B%u7597%u8239%2C%u7B26%u540818%u53F7%u51B3%u8BAE%28Mob-83%29%u7684%u8239%u8236%2C%u62D6%u5F15%u5E76%u4E14%u8239%u957F%3E200m%u6216%u8239%u5BBD%3E25m%2C%u758F%u6D5A%u6216%u6C34%u4E0B%u4F5C%u4E1A%2C%u6F5C%u6C34%u4F5C%u4E1A%2C%u53C2%u4E0E%u519B%u4E8B%u884C%u52A8%2C%u5E06%u8239%u822A%u884C%2C%u6E38%u8247%2C%u5730%u6548%u5E94%u8239%2C%u9AD8%u901F%u8239%2C%u5176%u4ED6%u7C7B%u578B%u7684%u8239%u8236%2C%u5176%u4ED6',
            '_elane_shipfilter_length': '0%2C40%2C41%2C80%2C81%2C120%2C121%2C160%2C161%2C240%2C241%2C320%2C321%2C9999',
            '_elane_shipfilter_sog': '0%2C1',
            '_elane_shipfilter_naviStatus': '0%2C1%2C2%2C3%2C4%2C5%2C6%2C7%2C8%2C15%2C255',
            '_elane_shipfilter_olength': '',
            '_elane_shipfilter_osog': '',
            '_elane_shipfilter_customsog': '',
            '_filter_flag': '-1',
            '_elane_shipfilter_one': '2',
            '_elane_shipfilter_country': '0%2C1%2C2',
            'tc_QX': '',
            'jfg': '28435863789c2726e8b008a4d57349db',
            'token': '344d132af4ad857436a2f881156de10c',
            'Hm_lvt_adc1d4b64be85a31d37dd5e88526cc47': '1772768211,1773038174',
            'Hm_lpvt_adc1d4b64be85a31d37dd5e88526cc47': '1773038174',
            'HMACCOUNT': '9348E0F4077C98C2',
            'shipxy_v3_history_serch': 's%u2606YUNGANGDIANTUOERHAO%u2606413541070%u260652%u2606MMSI%uFF1A413541070%7Cs%u2606YUNGANGDIANTUOJIUHAO%u2606412380820%u260652%u2606MMSI%uFF1A412380820',
            'SERVERID': '9d5fe2e75816d586ec8202c8a7e5ae11|1773038441|1773038172',
        }

    def fetch_position(self):
        """
        从shipxy.com API获取船舶位置数据并更新缓存
        """
        try:
            # 准备请求数据
            data = {
                'shipIDs': self.mmsi,
                'mmsi': self.mmsi,
            }

            # 生成签名
            gst = sign(data)

            # 准备请求头
            headers = {
                'accept': 'application/json, text/javascript, */*; q=0.01',
                'accept-language': 'zh-CN,zh;q=0.9,en;q=0.8',
                'cache-control': 'no-cache',
                'content-type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'origin': 'https://www.shipxy.com',
                'pragma': 'no-cache',
                'priority': 'u=1, i',
                'referer': 'https://www.shipxy.com/',
                's': gst["sign"],
                'sec-ch-ua': '"Not:A-Brand";v="99", "Google Chrome";v="145", "Chromium";v="145"',
                'sec-ch-ua-mobile': '?0',
                'sec-ch-ua-platform': '"Windows"',
                'sec-fetch-dest': 'empty',
                'sec-fetch-mode': 'cors',
                'sec-fetch-site': 'same-origin',
                't': gst["timestamp"],
                'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36',
                'x-requested-with': 'XMLHttpRequest',
            }

            # 发送POST请求
            response = requests.post(
                self.api_url,
                cookies=self.cookies,
                headers=headers,
                data=data,
                timeout=10
            )

            if response.status_code == 200:
                result = response.json()

                if result.get('status') == 0 and result.get('data'):
                    ship_data = result['data'][0]  # 获取第一条船舶数据

                    # 数据转换：
                    # sog / 514 为真正的航速（节）
                    # lon 和 lat 要除以1e6才是经纬度
                    # cog / 100 为航向（度）
                    lat = ship_data.get('lat', 0) / 1e6
                    lon = ship_data.get('lon', 0) / 1e6
                    ship_speed = ship_data.get('sog', 0) / 514.0  # 转换为节
                    cog = ship_data.get('cog', 0) / 100.0  # 转换为度

                    # 更新缓存
                    self.cache.update(
                        lat=lat,
                        lon=lon,
                        ship_speed=ship_speed,
                        cog=cog
                    )

                    print(f"[SHIPXY API] Position updated: lat={lat:.6f}, lon={lon:.6f}, speed={ship_speed:.2f}kn, cog={cog:.2f}°")
                else:
                    print(f"[SHIPXY API] Invalid response status: {result.get('status')}")
            else:
                print(f"[SHIPXY API] HTTP status: {response.status_code}")

        except requests.exceptions.Timeout:
            print("[SHIPXY API] Request timeout")
        except requests.exceptions.RequestException as e:
            print(f"[SHIPXY API] Request failed: {e}")
        except Exception as e:
            print(f"[SHIPXY API] Unexpected error: {e}")

    def start(self):
        """
        启动定时任务
        """
        if self.scheduler is None:
            self.scheduler = BackgroundScheduler()
            # 立即执行一次
            self.fetch_position()
            # 然后每隔interval秒执行一次
            self.scheduler.add_job(
                self.fetch_position,
                'interval',
                seconds=self.interval,
                id='ship_position_api'
            )
            self.scheduler.start()
            print(f"[SHIPXY API] Started, interval: {self.interval}s, MMSI: {self.mmsi}")

    def stop(self):
        """
        停止定时任务
        """
        if self.scheduler is not None:
            self.scheduler.shutdown()
            self.scheduler = None
            print("[SHIPXY API] Stopped")

    def get_cache(self):
        """
        获取缓存对象
        :return: ShipDataCache对象
        """
        return self.cache
