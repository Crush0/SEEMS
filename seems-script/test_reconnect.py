#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
WebSocket 重连机制测试脚本

用于测试指数退避和随机抖动算法的正确性
"""

import random
import time


class ReconnectTest:
    """重连机制测试类"""

    def __init__(self):
        # 复用 real time.py 中的相同参数
        self.initial_reconnect_delay = 1.0
        self.max_reconnect_delay = 300.0
        self.reconnect_backoff_factor = 1.5
        self.jitter_range = 0.3
        self.max_reconnect_attempts = 20

    def calculate_reconnect_delay(self, attempt):
        """
        计算重连延迟时间，使用指数退避和随机抖动

        Args:
            attempt: 当前重连尝试次数

        Returns:
            float: 延迟时间（秒）
        """
        # 指数退避计算
        delay = min(
            self.initial_reconnect_delay * (self.reconnect_backoff_factor ** attempt),
            self.max_reconnect_delay
        )

        # 添加随机抖动
        jitter = random.uniform(-self.jitter_range, self.jitter_range)
        final_delay = delay * (1 + jitter)

        # 确保延迟不为负数
        return max(0.1, final_delay)

    def test_delay_calculation(self, attempts=20):
        """测试延迟计算"""
        print("=" * 80)
        print("WebSocket 重连延迟测试")
        print("=" * 80)
        print(f"初始延迟: {self.initial_reconnect_delay}秒")
        print(f"最大延迟: {self.max_reconnect_delay}秒")
        print(f"退避因子: {self.reconnect_backoff_factor}")
        print(f"抖动范围: ±{self.jitter_range * 100}%")
        print("=" * 80)
        print()

        delays = []
        for i in range(attempts):
            base_delay = min(
                self.initial_reconnect_delay * (self.reconnect_backoff_factor ** i),
                self.max_reconnect_delay
            )
            actual_delay = self.calculate_reconnect_delay(i)
            delays.append(actual_delay)

            print(f"尝试 {i+1:2d}: 基础延迟={base_delay:6.2f}s, "
                  f"实际延迟={actual_delay:6.2f}s, "
                  f"抖动={((actual_delay/base_delay - 1) * 100):+6.1f}%")

        print()
        print("=" * 80)
        print("统计信息:")
        print(f"  平均延迟: {sum(delays) / len(delays):.2f}秒")
        print(f"  最小延迟: {min(delays):.2f}秒")
        print(f"  最大延迟: {max(delays):.2f}秒")
        print(f"  总延迟: {sum(delays):.2f}秒 ({sum(delays)/60:.2f}分钟)")
        print("=" * 80)

    def test_jitter_distribution(self, samples=1000):
        """测试抖动分布"""
        print()
        print("=" * 80)
        print("抖动分布测试（1000次采样）")
        print("=" * 80)

        # 测试第5次重连的抖动分布
        base_delay = min(
            self.initial_reconnect_delay * (self.reconnect_backoff_factor ** 5),
            self.max_reconnect_delay
        )

        jitters = []
        for _ in range(samples):
            delay = self.calculate_reconnect_delay(5)
            jitter = (delay / base_delay - 1) * 100
            jitters.append(jitter)

        # 统计分布
        jitter_min = min(jitters)
        jitter_max = max(jitters)
        jitter_avg = sum(jitters) / len(jitters)

        # 分区间统计
        bins = [-30, -20, -10, 0, 10, 20, 30]
        bin_counts = [0] * (len(bins) - 1)

        for jitter in jitters:
            for i in range(len(bins) - 1):
                if bins[i] <= jitter < bins[i + 1]:
                    bin_counts[i] += 1
                    break

        print(f"第5次重连的基础延迟: {base_delay:.2f}秒")
        print()
        print("抖动统计:")
        print(f"  最小抖动: {jitter_min:.2f}%")
        print(f"  最大抖动: {jitter_max:.2f}%")
        print(f"  平均抖动: {jitter_avg:.2f}%")
        print()
        print("抖动分布:")
        for i in range(len(bins) - 1):
            bar = '█' * (bin_counts[i] // 10)
            print(f"  [{bins[i]:+3d}%, {bins[i+1]:+3d}%): {bin_counts[i]:4d} {bar}")
        print("=" * 80)

    def test_backoff_growth(self, max_attempts=15):
        """测试指数退避增长"""
        print()
        print("=" * 80)
        print("指数退避增长曲线")
        print("=" * 80)

        print(f"{'尝试':<6} {'基础延迟':<12} {'最小延迟':<12} {'最大延迟':<12}")
        print("-" * 80)

        for i in range(max_attempts):
            base_delay = min(
                self.initial_reconnect_delay * (self.reconnect_backoff_factor ** i),
                self.max_reconnect_delay
            )

            # 计算带抖动的最小和最大延迟
            min_delay = base_delay * (1 - self.jitter_range)
            max_delay = base_delay * (1 + self.jitter_range)

            # 确保不小于0.1秒
            min_delay = max(0.1, min_delay)
            max_delay = max(0.1, max_delay)

            print(f"{i+1:<6} {base_delay:<12.2f} {min_delay:<12.2f} {max_delay:<12.2f}")

            # 如果达到最大延迟，后续都相同
            if base_delay >= self.max_reconnect_delay:
                print(f"... (后续尝试将保持最大延迟 {self.max_reconnect_delay}秒)")
                break

        print("=" * 80)

    def simulate_reconnection_scenario(self):
        """模拟重连场景"""
        print()
        print("=" * 80)
        print("模拟重连场景（连接成功后重置）")
        print("=" * 80)
        print()

        attempt = 0
        total_time = 0

        # 模拟失败5次后成功
        for i in range(5):
            delay = self.calculate_reconnect_delay(attempt)
            total_time += delay
            attempt += 1

            print(f"[尝试 {attempt}] 等待 {delay:.2f}秒... 连接失败 ❌")
            print(f"         累计等待: {total_time:.2f}秒")

        # 模拟成功
        print(f"[尝试 {attempt + 1}] 连接成功! ✅")
        print(f"         重置重连计数器")
        print()

        # 再次失败
        attempt = 0
        delay = self.calculate_reconnect_delay(attempt)
        print(f"[尝试 1] 等待 {delay:.2f}秒... 连接失败 ❌")
        print("         (计数器已重置，从头开始)")

        print()
        print("=" * 80)


def main():
    """主测试函数"""
    test = ReconnectTest()

    # 运行所有测试
    test.test_delay_calculation(attempts=15)
    test.test_jitter_distribution(samples=1000)
    test.test_backoff_growth(max_attempts=15)
    test.simulate_reconnection_scenario()

    print()
    print("✅ 所有测试完成!")


if __name__ == '__main__':
    main()
