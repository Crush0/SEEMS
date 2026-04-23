# WebSocket 重连机制说明

## 概述

WebSocket 重连机制已升级为使用 **指数退避 + 随机抖动** 算法，以实现更稳定和可靠的重连策略。这可以避免在网络问题或服务器重启时快速重连造成的服务器压力。

## 算法原理

### 指数退避 (Exponential Backoff)

每次重连失败后，等待时间会按指数增长：

```
delay = min(initial_delay × (backoff_factor ^ attempt), max_delay)
```

- **initial_delay**: 初始延迟时间（默认：1秒）
- **backoff_factor**: 退避因子（默认：1.5）
- **attempt**: 当前重连尝试次数
- **max_delay**: 最大延迟时间（默认：300秒）

### 随机抖动 (Jitter)

为了避免多个客户端同时重连导致"惊群效应"，在指数退避基础上添加随机抖动：

```
final_delay = delay × (1 + random.uniform(-jitter_range, jitter_range))
```

- **jitter_range**: 抖动范围（默认：±30%）

## 重连时间示例

假设使用默认配置（initial_delay=1s, backoff_factor=1.5, max_delay=300s, jitter=0.3）：

| 尝试次数 | 基础延迟 | 最终延迟（带抖动） |
|---------|---------|-----------------|
| 1       | 1.0s    | 0.7s - 1.3s     |
| 2       | 1.5s    | 1.1s - 2.0s     |
| 3       | 2.3s    | 1.6s - 2.9s     |
| 4       | 3.4s    | 2.4s - 4.4s     |
| 5       | 5.1s    | 3.6s - 6.6s     |
| 6       | 7.6s    | 5.3s - 9.9s     |
| 7       | 11.4s   | 8.0s - 14.8s    |
| 8       | 17.1s   | 12.0s - 22.2s   |
| 9       | 25.6s   | 17.9s - 33.3s   |
| 10      | 38.4s   | 26.9s - 50.0s   |
| ...     | ...     | ...             |
| 15+     | 300s    | 210s - 390s     |

## 配置参数

在 `realtime.py` 中的 `DataWebSocket.__init__()` 方法可以修改以下参数：

```python
self.initial_reconnect_delay = 1.0  # 初始重连延迟（秒）
self.max_reconnect_delay = 300.0  # 最大重连延迟（秒）
self.reconnect_backoff_factor = 1.5  # 指数退避因子
self.jitter_range = 0.3  # 随机抖动范围（±30%）
self.max_reconnect_attempts = 20  # 最大重连尝试次数（0表示无限制）
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|-----|------|--------|------|
| `initial_reconnect_delay` | float | 1.0 | 首次重连前的等待时间（秒） |
| `max_reconnect_delay` | float | 300.0 | 重连延迟的最大值（秒） |
| `reconnect_backoff_factor` | float | 1.5 | 每次重连延迟的乘数因子 |
| `jitter_range` | float | 0.3 | 随机抖动比例（0.0-1.0） |
| `max_reconnect_attempts` | int | 20 | 最大重连次数（0=无限重连） |

### 推荐配置

#### 快速重连（内网稳定环境）
```python
self.initial_reconnect_delay = 0.5
self.max_reconnect_delay = 30.0
self.reconnect_backoff_factor = 1.2
self.jitter_range = 0.2
```

#### 标准配置（生产环境）
```python
self.initial_reconnect_delay = 1.0
self.max_reconnect_delay = 300.0
self.reconnect_backoff_factor = 1.5
self.jitter_range = 0.3
```

#### 保守配置（不稳定网络）
```python
self.initial_reconnect_delay = 2.0
self.max_reconnect_delay = 600.0
self.reconnect_backoff_factor = 2.0
self.jitter_range = 0.4
```

## 使用方法

### 基本使用

```python
from realtime import DataWebSocket

# 创建WebSocket实例
ws_instance = DataWebSocket(args_dict, position_cache=None)

# 启动连接
ws_instance.start_ws()
```

### 查看重连状态

```python
# 获取重连统计信息
stats = ws_instance.get_reconnect_stats()
print(f"重连次数: {stats['reconnect_attempts']}")
print(f"下次重连延迟: {stats['next_reconnect_delay']:.2f}秒")
print(f"是否允许重连: {stats['should_reconnect']}")
```

### 手动停止重连

```python
# 停止WebSocket并禁用自动重连
ws_instance.stop()
```

### 自定义重连参数

可以在 `realtime.py` 中修改 `__init__` 方法的参数，或者创建子类：

```python
class CustomWebSocket(DataWebSocket):
    def __init__(self, args, position_cache=None):
        super().__init__(args, position_cache)
        # 自定义重连参数
        self.initial_reconnect_delay = 2.0
        self.max_reconnect_delay = 600.0
        self.reconnect_backoff_factor = 2.0
        self.max_reconnect_attempts = 50
```

## 日志输出示例

连接断开时的日志：

```
### closed ###
status_code: 1006, msg:
### ===closed=== ###

[RECONNECT] Attempt 1/20
[RECONNECT] Waiting 1.15 seconds before reconnecting...
[RECONNECT] Attempting to reconnect...

### opened ###
[RECONNECT] Connection established! Reset reconnection attempts.
```

## 注意事项

1. **线程安全**: 重连机制使用锁 (`threading.Lock`) 防止并发重连
2. **优雅关闭**: 使用 `stop()` 方法可以优雅停止并禁用重连
3. **信号处理**: `run.py` 已集成信号处理器，支持 Ctrl+C 优雅退出
4. **重连限制**: 设置 `max_reconnect_attempts` 可避免无限重连
5. **延迟保证**: 即使带抖动，延迟也不会小于 0.1 秒

## 故障排查

### 问题：连接频繁断开重连

**可能原因**：
- 网络不稳定
- 服务器负载过高
- 心跳超时

**解决方案**：
- 增加 `initial_reconnect_delay`
- 增大 `reconnect_backoff_factor`

### 问题：重连等待时间过长

**可能原因**：
- `max_reconnect_delay` 设置过大
- 多次重连失败

**解决方案**：
- 降低 `max_reconnect_delay`
- 检查网络和服务器状态

### 问题：多个客户端同时重连

**可能原因**：
- 未启用随机抖动
- 抖动范围太小

**解决方案**：
- 确保 `jitter_range > 0`
- 增大抖动范围到 0.3-0.5

## 参考资源

- [AWS Exponential Backoff and Jitter](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/)
- [Exponential Backoff In Distributed Systems](https://cloud.google.com/architecture/rate-limiting-strategies-techniques)
- [WebSocket Best Practices](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
