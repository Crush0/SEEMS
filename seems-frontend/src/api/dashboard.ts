import axios from 'axios';
import { ShipSocData } from '@/store/modules/ship-data/type';
import { WorkStatus } from '@/types/global';
import { ShipInfo } from './flow';

export interface ShipDataRecord {
  time: string;
  leftBatteryCapacity: number;
  rightBatteryCapacity: number;
  latitude: number;
  longitude: number;
  direction: number;
  shipId: number;
  item: number;
  workStatus: string;
  speed: number;
}

export interface ShipWorkConditionDuration {
  [key: WorkStatus | string]: number;
}

export interface ShipDataRecordResp {
  navData?: ShipDataRecord[];
  socData?: ShipSocData[];
  workConditionDuration?: ShipWorkConditionDuration;
}

export interface ShipInfoResp {
  shipInfo: ShipInfo;
}

export function queryShipNavigationData() {
  return axios.get<ShipDataRecordResp>('/api/ship-data/get-nearly-nav-data');
}

export function queryShipInfo() {
  return axios.get<ShipInfoResp>('/api/ship-data/get-ship-info');
}

export function queryShipNavigationDataByDateTimeBetween(params: {
  start: string;
  end: string;
}) {
  return axios.get<ShipDataRecordResp>(
    '/api/ship-data/get-nav-data-by-time-range',
    {
      params,
    }
  );
}

export function queryWorkConditionDurationByDateTimeBetween(params: {
  start: string | Date;
  end: string | Date;
}) {
  return axios.get<ShipDataRecordResp>(
    '/api/ship-data/get-work-condition-duration-by-time-range',
    {
      params,
    }
  );
}

export function queryShipSocDataByDateTimeBetween(params: {
  start: string | Date;
  end: string | Date;
}) {
  return axios.get<ShipDataRecordResp>(
    '/api/ship-data/get-soc-data-by-time-range',
    {
      params,
    }
  );
}
