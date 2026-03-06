import { WorkStatus } from '@/types/global';

export interface ShipDataRecord {
  time: string;
  leftBatteryCapacity: number;
  rightBatteryCapacity: number;
  latitude: number;
  longitude: number;
  shipId: number;
  item: number;
  workStatus: string;
  speed: number;
}

export interface ShipSocData {
  time: string;
  soc?: number;
  voltage?: number;
  electricity?: number;
  power?: number;
  temperature?: number;
  position?: string;
}

export interface Position {
  latitude?: number;
  longitude?: number;
}

// eslint-disable-next-line no-shadow
export enum PropellerWorkStatus {
  RUNNING = 'RUNNING',
  STOPPED = 'STOPPED',
  ERROR = 'ERROR',
  UNKNOWN = 'UNKNOWN',
}

export interface PropellerData {
  // 推进器转速
  rpm?: number;
  // 推进器频率
  degrees?: number;
  // 推进器功率
  power?: number;
  // 推进器状态
  status?: PropellerWorkStatus;
}

export interface RealTimeData {
  time: number;
  direction?: number;
  position?: Position;
  speed?: number;
  leftBatteryCapacity?: number;
  rightBatteryCapacity?: number;
  // 左推进器
  leftPropeller?: PropellerData;
  // 右推进器
  rightPropeller?: PropellerData;
  sailRange?: number;
  sailDuration?: number;
  powerDissipation?: number;
  leftBatteryAlarm?: boolean[];
  rightBatteryAlarm?: boolean[];
  totalSailRange?: number;
  windDirection?: number;
  windSpeed?: number;
  workStatus?: WorkStatus;
}
