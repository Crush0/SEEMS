import math
from decimal import Decimal
from logger import logger
import numpy as np
from geopy.distance import great_circle

from db.sql.model import WorkStatus
from db.sql.model.PropellerDataModel import PropellerWorkStatus

PORT_POSITION = (34.739735, 119.444222)  # 定义港口位置，表示一个固定的经纬度坐标
KNOT_CONVERSION_FACTOR = 1.852  # 海里转换为千米的系数（1海里 = 1.852公里）

INF = math.inf  # 定义一个极大数值，用于在特定情况下表示无效值或极限值



# 稳定的Sigmoid函数，避免在计算时出现溢出或者不稳定
def stable_sigmoid(x, k=10 / 1):
    """
    稳定的Sigmoid函数实现
    :param x: 输入值
    :param k: Sigmoid函数的斜率
    :return: 返回Sigmoid计算结果
    """
    return max(Decimal(1), min(Decimal(0), Decimal(1.0) / (Decimal(1.0) + np.exp(-Decimal(k) * Decimal(x) * Decimal(1.0)))))


# 判断当前坐标是否在港口范围内
def is_within_range(coord1: tuple, coord2: tuple = PORT_POSITION, radius_km: float = 0.1) -> bool:
    """
    判断坐标coord1是否在coord2的范围内
    :param coord1: 第一个坐标 (latitude, longitude)
    :param coord2: 第二个坐标 (latitude, longitude)，默认是港口位置
    :param radius_km: 范围（公里），默认值为3公里
    :return: 如果在范围内返回True，否则返回False
    """
    distance = great_circle(coord1, coord2).kilometers  # 计算两坐标之间的距离，单位为公里
    return distance <= radius_km  # 判断距离是否在范围内


def work_condition_handler(**kwargs):
    """
    根据提供的模型数据来判断船舶的工作状态
    :param kwargs: 提供的数据模型，包括推进器数据、电池数据和GPS日志
    """
    models = kwargs['models']  # 获取所有相关的模型数据

    # 获取推进器数据和GPS日志
    left_propeller_data = models["left_propeller_data"]
    right_propeller_data = models["right_propeller_data"]
    right_battery_data = models["right_battery_log"]
    left_battery_data = models["left_battery_log"]
    gps_log = models["gps_log"]

    # 计算能耗 kwh，P = V * I (瓦特)
    V = (right_battery_data.voltage + left_battery_data.voltage) / 2.0  # 平均电压
    I = (right_battery_data.electricity + left_battery_data.electricity) / 2.0  # 平均电流
    E_SOC = ((V * I) / 1e3) * 1.0  # 计算能量使用量（单位为kWh）

    # 计算当前航速（单位为米/小时）
    location = (gps_log.latitude, gps_log.longitude)  # 获取当前船舶的经纬度坐标
    speed_knots = gps_log.speed  # 获取当前航速，单位为海里/小时
    speed_m_h = speed_knots * KNOT_CONVERSION_FACTOR * 1000  # 转换为米/小时

    # 计算单位距离的电能使用量（EPD，单位：kWh/m）
    if speed_knots != 0:
        EPD = Decimal(abs(E_SOC)) / Decimal(speed_m_h)  # 如果航速不为零，计算EPD
    else:
        EPD = INF  # 如果航速为0，设置EPD为极大数值（无效值）

    stable_sigmoid_EPD = stable_sigmoid(EPD)  # 计算EPD的Sigmoid值，用于判断工作状态的稳定性

    # 判断工作状态
    if ((is_within_range(location) and speed_m_h <= 0 and (
            left_propeller_data.status == PropellerWorkStatus.STOPPED.value or right_propeller_data.status == PropellerWorkStatus.STOPPED.value))):
        # 如果在港口范围内且航速很低，或者电池电量SOC大于阈值，则认为是停港状态
        if E_SOC > 1e-2:
            work_condition = WorkStatus.CHARGING
        else:
            work_condition = WorkStatus.STOPPING_AT_PORT
    else:
        # 判断是否处于拖拽状态
        if speed_m_h > 0 and stable_sigmoid_EPD >= 0.75 and (
                left_propeller_data.status == PropellerWorkStatus.RUNNING.value or right_propeller_data.status == PropellerWorkStatus.RUNNING.value):
            work_condition = WorkStatus.DRAGGING
        # 判断是否处于航行状态
        elif speed_m_h > 0 and stable_sigmoid_EPD < 0.75 and (
                left_propeller_data.status == PropellerWorkStatus.RUNNING.value or right_propeller_data.status == PropellerWorkStatus.RUNNING.value):
            work_condition = WorkStatus.HOVERING
        else:
            # 如果无法确定工况，则为未知状态
            work_condition = WorkStatus.IDLE

    # 输出调试信息，显示当前的计算过程
    logger.info(msg=
                f'E_SOC: {E_SOC}, 当前航速：{speed_m_h}m/h， 是否在港口范围内:{is_within_range(location)}, EPD: {stable_sigmoid_EPD},判断工况:{work_condition.value}')

    # 更新gps_log的工作状态
    gps_log.workStatus = work_condition.value
