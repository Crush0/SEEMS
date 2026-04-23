import axios from 'axios';

export interface VoyageLog {
  id?: string;
  shipId: string | number;
  departure?: string;
  arrival?: string;
  startTime?: string;
  endTime?: string;
  voyageDistance?: number;
  sailingTime?: number;
  voyagePowerConsumption?: number;
}

export interface VoyageStatistics {
  totalVoyages: number;
  totalDistance: number;
  totalPowerConsumption: number;
  totalTime: number;
  averageDistance: number;
  averagePowerConsumption: number;
  averageTime: number;
}

export interface VoyageListParams {
  current?: number;
  pageSize?: number;
  startTime?: string;
  endTime?: string;
}

export function queryVoyageList(params: VoyageListParams) {
  return axios.get('/api/voyage/list', { params });
}

export function getVoyageDetail(id: string) {
  return axios.get(`/api/voyage/${id}`);
}

export function getVoyageStatistics(params: { startTime?: string; endTime?: string }) {
  return axios.get<VoyageStatistics>('/api/voyage/statistics', { params });
}
