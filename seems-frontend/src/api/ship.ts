import axios from 'axios';

export interface ShipBaseInfo {
  id: number;
  name: string;
  [key: string]: any;
}

/**
 * 获取当前用户的船舶信息
 */
export function getShipInfo() {
  return axios.get<ShipBaseInfo>('/api/ship/info');
}
