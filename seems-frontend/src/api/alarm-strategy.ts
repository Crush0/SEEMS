import axios from 'axios';

export interface ReceiverConfig {
  receiverType: 'ROLE' | 'USER';
  receiverId?: number;
  receiverRole?: string;
}

export interface AlarmStrategyVO {
  id: number;
  shipId?: number;
  shipName?: string;
  alarmType: string;
  alarmTypeName: string;
  alarmLevel: string;
  alarmLevelName: string;
  triggerCondition: 'LESS_THAN' | 'GREATER_THAN' | 'BETWEEN' | 'EQUAL';
  thresholdValue: number;
  thresholdValue2?: number;
  triggerTiming: 'IMMEDIATE' | 'DURATION';
  durationSeconds?: number;
  enableNotification: boolean;
  titleTemplate: string;
  contentTemplate: string;
  isEnabled: boolean;
  priority: number;
  createDate: string;
  updateDate: string;
  receivers: ReceiverInfo[];
}

export interface ReceiverInfo {
  id: number;
  receiverType: 'ROLE' | 'USER';
  receiverId?: number;
  receiverRole?: string;
  receiverName?: string;
}

export interface CreateAlarmStrategyRequest {
  shipId?: number;
  alarmType: string;
  alarmLevel: string;
  triggerCondition: 'LESS_THAN' | 'GREATER_THAN' | 'BETWEEN' | 'EQUAL';
  thresholdValue: number;
  thresholdValue2?: number;
  triggerTiming: 'IMMEDIATE' | 'DURATION';
  durationSeconds?: number;
  enableNotification: boolean;
  titleTemplate: string;
  contentTemplate: string;
  priority?: number;
  receivers: ReceiverConfig[];
}

export interface UpdateAlarmStrategyRequest extends CreateAlarmStrategyRequest {
  id: number;
}

export interface QueryAlarmStrategyRequest {
  shipId?: number;
  alarmType?: string;
  isEnabled?: boolean;
  current: number;
  pageSize: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 创建报警策略
 */
export function createStrategy(data: CreateAlarmStrategyRequest) {
  return axios.post<number>('/api/alarm-strategy/create', data);
}

/**
 * 更新报警策略
 */
export function updateStrategy(data: UpdateAlarmStrategyRequest) {
  return axios.post<boolean>('/api/alarm-strategy/update', data);
}

/**
 * 删除报警策略
 */
export function deleteStrategy(strategyId: number) {
  return axios.post<boolean>('/api/alarm-strategy/delete', null, {
    params: { strategyId }
  });
}

/**
 * 查询报警策略列表
 */
export function queryStrategyList(data: QueryAlarmStrategyRequest) {
  return axios.post<PageResult<AlarmStrategyVO>>('/api/alarm-strategy/list', data);
}

/**
 * 获取报警策略详情
 */
export function getStrategyDetail(strategyId: number) {
  return axios.get<AlarmStrategyVO>('/api/alarm-strategy/detail', {
    params: { strategyId }
  });
}

/**
 * 启用/禁用报警策略
 */
export function enableStrategy(strategyId: number, enabled: boolean) {
  return axios.post<boolean>('/api/alarm-strategy/enable', null, {
    params: { strategyId, enabled }
  });
}

/**
 * 获取有效的报警策略
 */
export function getEffectiveStrategy(shipId: number, alarmType: string) {
  return axios.get<AlarmStrategyVO>('/api/alarm-strategy/effective', {
    params: { shipId, alarmType }
  });
}

/**
 * 报警类型列表
 */
export const ALARM_TYPES = [
  { value: 'LOW_BATTERY', label: '低电量报警' },
  { value: 'HIGH_TEMPERATURE', label: '高温报警' },
  { value: 'GPS_LOST', label: 'GPS信号丢失' },
  { value: 'PROPULSION_FAILURE', label: '推进器故障' },
  { value: 'WEATHER_WARNING', label: '天气预警' },
  { value: 'SYSTEM_ERROR', label: '系统错误' },
];

/**
 * 报警级别列表
 */
export const ALARM_LEVELS = [
  { value: 'INFO', label: '信息' },
  { value: 'WARNING', label: '警告' },
  { value: 'ERROR', label: '错误' },
  { value: 'CRITICAL', label: '严重' },
];

/**
 * 触发条件列表
 */
export const TRIGGER_CONDITIONS = [
  { value: 'LESS_THAN', label: '低于' },
  { value: 'GREATER_THAN', label: '高于' },
  { value: 'BETWEEN', label: '区间' },
  { value: 'EQUAL', label: '等于' },
];

/**
 * 触发时机列表
 */
export const TRIGGER_TIMINGS = [
  { value: 'IMMEDIATE', label: '立即触发' },
  { value: 'DURATION', label: '持续一段时间后触发' },
];

/**
 * 接收人类型列表
 */
export const RECEIVER_TYPES = [
  { value: 'ROLE', label: '角色组' },
  { value: 'USER', label: '具体用户' },
];

/**
 * 角色列表
 */
export const ROLES = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'OPERATOR', label: '操作员' },
  { value: 'USER', label: '普通用户' },
];
