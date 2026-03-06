from datetime import datetime

import requests
requests.packages.urllib3.disable_warnings()
from exceptions import WeatherException

def convert_to_datetime(date_str):
    # 定义日期字符串的格式
    date_format = "%Y年%m月%d日%H时"
    # 将字符串转换为 datetime 对象
    dt = datetime.strptime(date_str, date_format)
    return dt

def get_weather():
    weather_data = {}

    headers = {
        'Accept': 'application/json, text/plain, */*',
        'Accept-Language': 'zh-CN,zh;q=0.9',
        'Connection': 'keep-alive',
        'Content-Type': 'application/x-www-form-urlencoded',
        'Origin': 'https://www.oceanguide.org.cn',
        'Referer': 'https://www.oceanguide.org.cn/DomesticPortFor',
        'Sec-Fetch-Dest': 'empty',
        'Sec-Fetch-Mode': 'cors',
        'Sec-Fetch-Site': 'same-origin',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'sec-ch-ua': '"Chromium";v="130", "Google Chrome";v="130", "Not?A_Brand";v="99"',
        'sec-ch-ua-mobile': '?0',
        'sec-ch-ua-platform': '"Windows"',
    }

    data = 'space=119.39060946432747,34.73180135313784'

    response = requests.post('https://www.oceanguide.org.cn/hyyj2/smartGrid/gridReport', headers=headers, data=data, verify=False)

    year = datetime.now().year
    month = datetime.now().month
    if response.status_code == 200:
        json_data = response.json()
        if json_data["success"] and json_data["msg"] == '1000':
            report = json_data['obj']['report']
            key = ['waterDirection', 'waterSpeed', 'waveDirection', 'waveHeight', 'windDirection', 'windSpeed']
            for k in key:
                report_data = report[k]
                weather_data[k] = {
                    "data": report_data['data'],
                    'time': [convert_to_datetime(f"{year}年{month}月{i}") for i in report_data["time"]]
                }
            weather_data['updateTime'] = convert_to_datetime(f"{year}年{month}月{json_data['obj']['report']['waterDirection']['time'][0]}")
            return weather_data
        else:
            raise WeatherException("获取天气数据失败")
    else:
        raise WeatherException("获取天气数据失败")

if __name__ == '__main__':
    print(get_weather())