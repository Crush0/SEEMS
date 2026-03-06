import axios from 'axios';

export interface Report {
  shipId: number;
  time: string;
  theorySpeed: number;
  actualSpeed: number;
  direction: number;
  longitude: number;
  latitude: number;
  windDirection: number;
  windSpeed: number;
  actualVoyage: number;
  totalVoyage: number;
  slidingRate: number;
}

export interface NoonReportParams {
  current: number;
  pageSize: number;
  createdTime?: string[];
}

export interface ReportParams {
  startDate: string;
  endDate: string;
  reportType: 'daily' | 'weekly' | 'monthly' | 'yearly' | 'quarterly';
}

export interface NoonReportResponse {
  list: Report[];
  total: number;
}

export function generateReport() {
  return axios.get<Report>('/api/report/generate');
}

export function updateOrInsertReport(report: Report) {
  return axios.post('/api/report/update-or-insert', report);
}

export function queryReport(params: NoonReportParams) {
  return axios.post<NoonReportResponse>('/api/report/query', params);
}

export function queryHistoryReport(data: ReportParams) {
  return axios.post('/api/report/query-history-report', data);
}
