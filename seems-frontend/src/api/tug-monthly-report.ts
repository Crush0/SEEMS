import axios from 'axios';

/**
 * 拖轮月度报表查询参数
 */
export interface TugMonthlyReportQuery {
  shipId: number;
  year: number;
  month: number;
}

/**
 * 每日能耗数据
 */
export interface DailyEnergyConsumption {
  date: string;
  energyConsumption: number;
  operations: number;
  voyageDistance: number;
}

/**
 * 拖轮月度报表数据
 */
export interface TugMonthlyReport {
  shipId: number;
  shipName: string;
  year: number;
  month: number;
  totalOperations: number;
  totalOperationHours: number;
  averageOperationDuration: number;
  totalVoyageDistance: number;
  totalPowerConsumption: number;
  averagePowerConsumptionPerOperation: number;
  powerConsumptionPerNauticalMile: number;
  dailyEnergyConsumptions: DailyEnergyConsumption[];
}

/**
 * 生成拖轮月度报表
 */
export function generateTugMonthlyReport(query: TugMonthlyReportQuery) {
  return axios.post<TugMonthlyReport>('/api/tug-monthly-report/generate', query);
}
