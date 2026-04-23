# 报警日志系统使用说明

## 系统概述

报警日志系统用于监控拖轮运行过程中的各种异常情况，记录报警信息并通过消息系统及时通知相关人员。

当前已实现的报警策略：
- **低电量报警**：当船舶电池SOC低于15%时触发

## 系统架构

### 后端组件

#### 1. 实体类（Entity）
- **AlarmLog.java**：报警日志实体类
  - 报警类型：LOW_BATTERY、HIGH_TEMPERATURE、GPS_LOST、PROPULSION_FAILURE、WEATHER_WARNING、SYSTEM_ERROR
  - 报警级别：INFO（信息）、WARNING（警告）、ERROR（错误）、CRITICAL（严重）

#### 2. 数据访问层（Mapper）
- **AlarmLogMapper.java**：报警日志数据访问接口
  - 查询未处理报警数量
  - 查询指定时间范围内的报警
  - 查询最近的严重报警

#### 3. 数据传输对象（DTO）
- **CreateAlarmLogRequest.java**：创建报警请求
- **QueryAlarmLogRequest.java**：查询报警列表请求
- **AlarmLogVO.java**：报警日志视图对象
- **QueryAlarmLogResp.java**：查询报警列表响应

#### 4. 服务层（Service）
- **AlarmLogService.java**：报警服务接口
- **AlarmLogServiceImpl.java**：报警服务实现类
  - `createAlarm()`：创建报警日志
  - `queryAlarmList()`：查询报警列表
  - `markAsHandled()`：标记报警为已处理
  - `getUnhandledCount()`：获取未处理报警数量
  - `createLowSocAlarm()`：创建低电量报警（含防重复机制）
  - `sendAlarmNotification()`：发送实时报警通知
  - `sendAlarmMessageToAll()`：发送系统消息给所有用户

#### 5. 控制器（Controller）
- **AlarmLogController.java**：报警日志控制器
  - `POST /api/alarm/create`：创建报警日志
  - `POST /api/alarm/list`：查询报警列表
  - `POST /api/alarm/handle`：标记报警为已处理
  - `GET /api/alarm/unhandled-count`：获取未处理报警数量
  - `POST /api/alarm/low-soc`：创建SOC低电量报警

### Python脚本组件

#### realtime.py 更新
- 添加后端API配置
- 添加SOC阈值配置
- 实现 `check_battery_alarm()` 方法：检测电池SOC
- 实现 `create_soc_alarm()` 方法：创建低电量报警
- 在 `save_data()` 中调用SOC检测

#### config.yaml 配置
```yaml
# 后端API配置（用于报警系统）
backend_api:
  url: http://localhost:8080
  timeout: 5

# 报警系统配置
alarm:
  soc_threshold: 15.0  # SOC报警阈值（百分比）
```

## 工作流程

### 低电量报警流程

```
1. Python脚本接收WebSocket数据
   ↓
2. 保存电池日志到数据库
   ↓
3. 检查SOC是否低于阈值（15%）
   ↓
4. 调用后端API创建报警
   ↓
5. 后端创建报警日志记录
   ↓
6. 后端发送实时通知（Socket.IO）
   ↓
7. 后端发送系统消息给所有用户
   ↓
8. 用户在前端收到报警消息
```

### 防重复机制

为避免频繁报警，系统实现以下防重复机制：

1. **时间窗口检查**（后端）：
   - 检查最近10分钟内是否有未处理的低电量报警
   - 如果存在，跳过创建新报警

2. **本地缓存**（Python脚本）：
   - 使用 `last_alarm_time` 字典记录每个电池的最后报警时间
   - 10分钟内不重复调用后端API

## API使用示例

### 1. 创建报警日志

```bash
POST /api/alarm/create
Content-Type: application/json

{
  "shipId": 2029764200985022466,
  "alarmType": "LOW_BATTERY",
  "alarmLevel": "CRITICAL",
  "title": "电量严重不足",
  "content": "SOC仅为12%，请立即充电",
  "relatedDataId": 123456789
}
```

### 2. 查询报警列表

```bash
POST /api/alarm/list
Content-Type: application/json

{
  "shipId": 2029764200985022466,
  "alarmType": "LOW_BATTERY",
  "isHandled": false,
  "current": 1,
  "pageSize": 10
}
```

### 3. 标记报警为已处理

```bash
POST /api/alarm/handle?alarmId=123456789
```

### 4. 获取未处理报警数量

```bash
GET /api/alarm/unhandled-count?shipId=2029764200985022466
```

### 5. 创建低电量报警

```bash
POST /api/alarm/low-soc?shipId=2029764200985022466&socValue=12.5&relatedDataId=123456789
```

## 报警级别说明

| 级别 | 编码 | 优先级 | 说明 | 示例场景 |
|-----|------|--------|------|---------|
| 信息 | info | 1 | 一般信息 | SOC正常下降提醒 |
| 警告 | warning | 2 | 需要注意 | SOC低于30% |
| 错误 | error | 3 | 需要处理 | 传感器数据异常 |
| 严重 | critical | 4 | 紧急情况 | SOC低于15% |

## 报警类型说明

| 类型 | 编码 | 说明 |
|-----|------|------|
| 低电量报警 | low_battery | 电池SOC低于阈值 |
| 高温报警 | high_temperature | 电池温度过高 |
| GPS信号丢失 | gps_lost | GPS数据缺失 |
| 推进器故障 | propulsion_failure | 推进器异常 |
| 天气预警 | weather_warning | 恶劣天气提醒 |
| 系统错误 | system_error | 系统运行错误 |

## 消息通知

### 实时通知（Socket.IO）
- 通过Socket.IO实时推送报警信息
- 推送给该船舶的所有在线用户
- 前端可实时显示报警提示

### 系统消息
- 创建系统消息记录
- 发送给所有角色用户（ADMIN、OPERATOR、USER）
- 用户可在消息中心查看

### 消息格式示例

```json
{
  "title": "【拖轮1】电量严重不足",
  "content": "船舶 拖轮1 当前电池SOC仅为 14.5%，低于安全阈值15%，请立即充电！\n报警时间: 2026-03-10 15:30:00\n建议措施: 立即返回充电站或寻找充电设施",
  "type": "notice"
}
```

## 前端集成（待实现）

### 1. 报警列表页面
```typescript
// 查询报警列表
const fetchAlarms = async () => {
  const { data } = await queryAlarmList({
    shipId: currentShipId,
    isHandled: false,
    current: 1,
    pageSize: 20
  });
  // 显示报警列表
};
```

### 2. 实时报警通知
```typescript
// 监听Socket.IO的报警事件
socket.on('alarm', (alarm) => {
  // 显示报警通知
  notification.warning({
    title: alarm.title,
    content: alarm.content,
    duration: 0  // 不自动关闭
  });
});
```

### 3. 标记已处理
```typescript
// 标记报警为已处理
const markAsHandled = async (alarmId: string) => {
  await markAlarmAsHandled(alarmId);
  // 刷新列表
};
```

## 配置说明

### Python脚本配置（config.yaml）

```yaml
# SOC报警阈值
alarm:
  soc_threshold: 15.0  # 可根据实际情况调整（建议10%-20%）

# 后端API地址
backend_api:
  url: http://localhost:8080  # 根据实际部署地址修改
  timeout: 5  # 请求超时时间（秒）
```

### 调整建议

1. **SOC阈值调整**：
   - 内河短途作业：可设为10-15%
   - 沿海长途作业：建议设为20-25%
   - 恶劣海况：建议提高至30%

2. **报警频率控制**：
   - 默认10分钟内不重复报警
   - 可通过修改 `realtime.py` 中的 `600`（秒）调整

3. **消息通知范围**：
   - 当前发送给所有用户
   - 可根据需要修改为仅发送给特定角色

## 测试方法

### 1. 模拟低电量报警

在Python脚本中临时修改SOC值：

```python
# 在 save_data() 方法中，模拟低SOC
left_battery_log.soc = 12.0  # 设置为低于阈值的值
```

### 2. 手动触发报警

使用Postman或curl测试API：

```bash
curl -X POST "http://localhost:8080/api/alarm/low-soc" \
  -H "Content-Type: application/json" \
  -d '{
    "shipId": 2029764200985022466,
    "socValue": 12.5,
    "relatedDataId": 123456789
  }'
```

### 3. 查看报警日志

```bash
# 查询数据库
mysql> SELECT * FROM alarm_log WHERE ship_id = 2029764200985022466 ORDER BY create_date DESC LIMIT 10;

# 查询未处理报警
mysql> SELECT * FROM alarm_log WHERE is_handled = 0 AND ship_id = 2029764200985022466;
```

## 数据库表结构

```sql
CREATE TABLE alarm_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ship_id BIGINT NOT NULL COMMENT '船舶ID',
    alarm_type VARCHAR(50) NOT NULL COMMENT '报警类型',
    alarm_level VARCHAR(50) NOT NULL COMMENT '报警级别',
    title VARCHAR(200) NOT NULL COMMENT '报警标题',
    content TEXT NOT NULL COMMENT '报警内容',
    is_handled TINYINT(1) DEFAULT 0 COMMENT '是否已处理',
    handle_time TIMESTAMP NULL COMMENT '处理时间',
    handler_id BIGINT COMMENT '处理人ID',
    related_data_id BIGINT COMMENT '关联数据ID',
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_ship_id (ship_id),
    INDEX idx_alarm_type (alarm_type),
    INDEX idx_alarm_level (alarm_level),
    INDEX idx_is_handled (is_handled),
    INDEX idx_create_date (create_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警日志表';
```

## 扩展新报警类型

### 示例：添加高温报警

#### 1. 在Python脚本中添加检测

```python
def check_temperature_alarm(self, left_battery, right_battery, gps_log_id):
    """检测电池温度并触发报警"""
    TEMP_THRESHOLD = 60.0  # 温度阈值（摄氏度）

    try:
        if left_battery.temperature > TEMP_THRESHOLD:
            self.create_temperature_alarm(left_battery.temperature, left_battery.position, gps_log_id)

        if right_battery.temperature > TEMP_THRESHOLD:
            self.create_temperature_alarm(right_battery.temperature, right_battery.position, gps_log_id)
    except Exception as e:
        print(f"[ALARM] Temperature alarm check failed: {e}")
```

#### 2. 调用后端API

```python
def create_temperature_alarm(self, temp_value, battery_position, gps_log_id):
    """创建高温报警"""
    try:
        api_url = f"{self.backend_api_url}/api/alarm/create"
        payload = {
            'shipId': self.args['ship_id'],
            'alarmType': 'HIGH_TEMPERATURE',
            'alarmLevel': 'CRITICAL',
            'title': f'电池温度过高',
            'content': f'{battery_position} 电池温度达到 {temp_value:.1f}°C，超过安全阈值60°C',
            'relatedDataId': gps_log_id
        }
        # ... 发送请求
```

## 常见问题

### Q1: 报警没有触发？
**A**: 检查以下几点：
1. 确认后端服务正在运行
2. 检查config.yaml中的backend_api.url配置是否正确
3. 查看Python脚本日志，确认API调用是否成功
4. 检查SOC值是否确实低于阈值

### Q2: 重复收到相同报警？
**A**: 系统已实现防重复机制：
- 后端检查10分钟内是否有未处理的相同报警
- Python脚本本地缓存也限制10分钟内不重复调用
- 如果仍有重复，检查系统时间是否准确

### Q3: 如何调整报警阈值？
**A**: 修改config.yaml：
```yaml
alarm:
  soc_threshold: 20.0  # 修改为新的阈值
```

### Q4: 消息发送失败怎么办？
**A**: 检查：
1. Socket.IO服务是否正常运行（端口33000）
2. 用户是否在线
3. 查看后端日志中的错误信息
4. 报警日志仍会保存到数据库，不影响数据记录

## 维护建议

1. **定期清理历史报警**：
   ```sql
   -- 删除3个月前已处理的报警
   DELETE FROM alarm_log
   WHERE is_handled = 1
   AND create_date < DATE_SUB(NOW(), INTERVAL 3 MONTH);
   ```

2. **统计报警频率**：
   ```sql
   -- 查看最近7天的报警统计
   SELECT alarm_type, alarm_level, COUNT(*) as count
   FROM alarm_log
   WHERE create_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)
   GROUP BY alarm_type, alarm_level
   ORDER BY count DESC;
   ```

3. **监控未处理报警**：
   ```sql
   -- 查看超过24小时未处理的报警
   SELECT * FROM alarm_log
   WHERE is_handled = 0
   AND create_date < DATE_SUB(NOW(), INTERVAL 24 HOUR);
   ```

## 相关文件清单

### 后端文件
```
seems-backend/src/main/java/cn/edu/just/ytc/seems/
├── pojo/entity/AlarmLog.java
├── pojo/dto/
│   ├── CreateAlarmLogRequest.java
│   ├── QueryAlarmLogRequest.java
│   ├── AlarmLogVO.java
│   └── QueryAlarmLogResp.java
├── mapper/AlarmLogMapper.java
├── service/
│   ├── AlarmLogService.java
│   └── Impl/AlarmLogServiceImpl.java
└── controller/AlarmLogController.java
```

### Python脚本文件
```
seems-script/
├── realtime.py（已更新）
└── config.yaml（已更新）
```

## 更新日志

### v1.0.0 (2026-03-10)
- ✅ 创建报警日志实体和数据库表
- ✅ 实现报警日志CRUD功能
- ✅ 实现SOC低电量报警检测
- ✅ 集成消息系统通知
- ✅ 实现防重复报警机制
- ✅ 添加Socket.IO实时推送
- ✅ 完善API文档和配置说明
