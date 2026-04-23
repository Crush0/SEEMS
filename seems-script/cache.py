#!/usr/bin/env python3
# _*_ coding:utf-8 _*_
"""
缓存类，用于保存API获取的船舶位置和航速数据
"""
import threading
import time


class ShipDataCache:
    """船舶数据缓存类"""

    def __init__(self):
        """初始化缓存"""
        self._lock = threading.Lock()
        self._data = {
            'lat': 0.0,        # 纬度
            'lon': 0.0,        # 经度
            'ship_speed': 0.0, # 航速
            'cog': 0.0,        # 航向
            'update_time': 0   # 更新时间戳
        }

    def update(self, lat=None, lon=None, ship_speed=None, cog=None):
        """
        更新缓存数据
        :param lat: 纬度
        :param lon: 经度
        :param ship_speed: 航速
        :param cog: 航向
        """
        with self._lock:
            if lat is not None:
                self._data['lat'] = lat
            if lon is not None:
                self._data['lon'] = lon
            if ship_speed is not None:
                self._data['ship_speed'] = ship_speed
            if cog is not None:
                self._data['cog'] = cog
            self._data['update_time'] = time.time()

    def get(self):
        """
        获取缓存数据
        :return: 包含位置和航速数据的字典
        """
        with self._lock:
            return self._data.copy()

    def is_valid(self, max_age=10):
        """
        检查缓存数据是否有效（未过期）
        :param max_age: 最大有效期（秒），默认10秒
        :return: 数据是否有效
        """
        with self._lock:
            return (time.time() - self._data['update_time']) < max_age

    def get_position_speed(self):
        """
        获取位置和航速数据（便捷方法）
        :return: (lat, lon, ship_speed, cog)
        """
        with self._lock:
            return (
                self._data['lat'],
                self._data['lon'],
                self._data['ship_speed'],
                self._data['cog']
            )
