"""
报警策略客户端
用于与后端API交互，查询和应用报警策略
"""

import requests
import json
import logging
import time
from typing import Optional, Dict, Any, List
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)


class AlarmStrategyClient:
    """报警策略客户端"""

    def __init__(self, base_url: str = "http://localhost:8080", timeout: int = 5):
        """
        初始化客户端

        Args:
            base_url: 后端API基础URL
            timeout: 请求超时时间（秒）
        """
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout
        self.api_prefix = "/api/alarm-strategy"
        self._strategy_cache = {}  # 策略缓存
        self._cache_expire_time = {}  # 缓存过期时间
        self._cache_ttl = 300  # 缓存生存时间（秒）

    def _get_url(self, endpoint: str) -> str:
        """获取完整的API URL"""
        return f"{self.base_url}{self.api_prefix}{endpoint}"

    def _request(self, method: str, endpoint: str, **kwargs) -> Optional[Dict]:
        """
        发送HTTP请求

        Args:
            method: 请求方法（GET, POST等）
            endpoint: API端点
            **kwargs: 其他请求参数

        Returns:
            响应数据字典，失败返回None
        """
        url = self._get_url(endpoint)
        try:
            response = requests.request(
                method,
                url,
                timeout=self.timeout,
                **kwargs
            )
            response.raise_for_status()

            data = response.json()
            if data.get('code') == 20000 or data.get('success'):
                return data.get('data')
            else:
                logger.error(f"API请求失败: {data.get('message', '未知错误')}")
                return None

        except requests.exceptions.Timeout:
            logger.error(f"API请求超时: {method} {url}")
            return None
        except requests.exceptions.RequestException as e:
            logger.error(f"API请求异常: {method} {url}, error={e}")
            return None
        except json.JSONDecodeError as e:
            logger.error(f"JSON解析失败: {e}")
            return None

    def get_effective_strategy(self, ship_id: int, alarm_type: str) -> Optional[Dict]:
        """
        获取有效的报警策略（带缓存）

        Args:
            ship_id: 船舶ID
            alarm_type: 报警类型

        Returns:
            策略字典，不存在返回None
        """
        cache_key = f"{ship_id}:{alarm_type}"

        # 检查缓存
        if cache_key in self._strategy_cache:
            expire_time = self._cache_expire_time.get(cache_key, 0)
            if time.time() < expire_time:
                logger.debug(f"从缓存获取策略: {cache_key}")
                return self._strategy_cache[cache_key]

        # 从API获取
        logger.debug(f"从API获取策略: {cache_key}")
        strategy = self._request(
            "GET",
            "/effective",
            params={"shipId": ship_id, "alarmType": alarm_type}
        )

        # 缓存结果
        if strategy:
            self._strategy_cache[cache_key] = strategy
            self._cache_expire_time[cache_key] = time.time() + self._cache_ttl
        else:
            # 如果没有策略，缓存空结果以避免频繁请求
            self._strategy_cache[cache_key] = None
            self._cache_expire_time[cache_key] = time.time() + 60  # 空结果缓存1分钟

        return strategy

    def apply_strategy(
        self,
        ship_id: int,
        alarm_type: str,
        value: float,
        related_data_id: Optional[int] = None,
        template_variables: Optional[Dict[str, Any]] = None
    ) -> bool:
        """
        应用报警策略（创建报警）

        Args:
            ship_id: 船舶ID
            alarm_type: 报警类型
            value: 当前值
            related_data_id: 关联数据ID（如GPS日志ID）
            template_variables: 模板变量（如battery_position, suggestion等）

        Returns:
            是否触发报警
        """
        payload = {
            "shipId": ship_id,
            "alarmType": alarm_type,
            "value": value,
            "relatedDataId": related_data_id,
            "templateVariables": template_variables or {}
        }

        result = self._request("POST", "/apply", json=payload)
        return result is True

    def check_trigger_condition(self, value: float, strategy: Dict) -> bool:
        """
        检查是否满足触发条件

        Args:
            value: 当前值
            strategy: 策略字典

        Returns:
            是否触发
        """
        if not strategy:
            return False

        trigger_condition = strategy.get('triggerCondition')
        if not trigger_condition:
            return False

        # 转换枚举值
        condition_code = trigger_condition.get('code') if isinstance(trigger_condition, dict) else trigger_condition
        threshold1 = strategy.get('thresholdValue')
        threshold2 = strategy.get('thresholdValue2')

        try:
            if condition_code == 'less_than':
                return value < float(threshold1)
            elif condition_code == 'greater_than':
                return value > float(threshold1)
            elif condition_code == 'between':
                if threshold2 is None:
                    return False
                return float(threshold1) <= value <= float(threshold2)
            elif condition_code == 'equal':
                return abs(value - float(threshold1)) < 0.01
            else:
                logger.warning(f"未知的触发条件: {condition_code}")
                return False
        except (ValueError, TypeError) as e:
            logger.error(f"触发条件检查失败: {e}")
            return False

    def clear_cache(self, ship_id: Optional[int] = None, alarm_type: Optional[str] = None):
        """
        清除缓存

        Args:
            ship_id: 船舶ID（None表示清除所有）
            alarm_type: 报警类型（None表示清除所有）
        """
        if ship_id is None or alarm_type is None:
            self._strategy_cache.clear()
            self._cache_expire_time.clear()
            logger.debug("清除所有策略缓存")
        else:
            cache_key = f"{ship_id}:{alarm_type}"
            if cache_key in self._strategy_cache:
                del self._strategy_cache[cache_key]
                del self._cache_expire_time[cache_key]
                logger.debug(f"清除策略缓存: {cache_key}")

    def check_and_trigger(
        self,
        ship_id: int,
        alarm_type: str,
        value: float,
        related_data_id: Optional[int] = None,
        template_variables: Optional[Dict[str, Any]] = None
    ) -> bool:
        """
        检查并触发报警（一站式方法）

        Args:
            ship_id: 船舶ID
            alarm_type: 报警类型
            value: 当前值
            related_data_id: 关联数据ID
            template_variables: 模板变量

        Returns:
            是否触发报警
        """
        # 获取策略
        strategy = self.get_effective_strategy(ship_id, alarm_type)
        if not strategy:
            logger.debug(f"未配置报警策略: ship_id={ship_id}, alarm_type={alarm_type}")
            return False

        # 检查触发条件
        if not self.check_trigger_condition(value, strategy):
            return False

        # 应用策略（创建报警）
        trigger_timing = strategy.get('triggerTiming')
        timing_code = trigger_timing.get('code') if isinstance(trigger_timing, dict) else trigger_timing

        # 如果是立即触发，直接创建报警
        if timing_code == 'immediate':
            return self.apply_strategy(ship_id, alarm_type, value, related_data_id, template_variables)

        # 如果是持续触发，需要追踪持续时间（由调用方处理）
        # 这里只返回True表示应该开始追踪
        return True


# 全局客户端实例
_alarm_client: Optional[AlarmStrategyClient] = None


def get_alarm_client() -> AlarmStrategyClient:
    """获取全局报警客户端实例"""
    global _alarm_client
    if _alarm_client is None:
        _alarm_client = AlarmStrategyClient()
    return _alarm_client


def init_alarm_client(base_url: str = "http://localhost:8080", timeout: int = 5):
    """
    初始化全局报警客户端

    Args:
        base_url: 后端API基础URL
        timeout: 请求超时时间（秒）
    """
    global _alarm_client
    _alarm_client = AlarmStrategyClient(base_url, timeout)
    logger.info(f"报警客户端初始化成功: {base_url}")
