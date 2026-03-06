import axios from 'axios';

export interface AnalysisParams {
  startDate: string;
  endDate: string;
}
export interface AnalysisData {
  analyzeTime: string;
  dailyEnergyConsumption?: number | string;
  dailyCarbonEmission?: number | string;
  preDistanceEnergyConsumption?: number;
  preHourEnergyConsumption?: number;
  preUnitWorkEnergyConsumption?: number;
  dailySailingDuration?: number;
  dailySailingDistance?: number;
}

export function queryDataAnalysis(data?: AnalysisParams) {
  return axios.post<AnalysisData[]>('/api/analysis/get', data);
}

export function analyzeImmediately() {
  return axios.get('/api/analysis/immediate');
}
