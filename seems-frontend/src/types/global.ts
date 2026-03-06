export interface AnyObject {
  [key: string]: unknown;
}

export interface Options {
  value: unknown;
  label: string;
}

export interface NodeOptions extends Options {
  children?: NodeOptions[];
}

export interface GetParams {
  body: null;
  type: string;
  url: string;
}

export interface PostData {
  body: string;
  type: string;
  url: string;
}

export interface Pagination {
  current: number;
  pageSize: number;
  total?: number;
}

export type TimeRanger = [string, string];

export interface GeneralChart {
  xAxis: string[];
  data: Array<{ name: string; value: number[] }>;

}

// eslint-disable-next-line no-shadow
export enum WorkStatus {
  HOVERING = 'HOVERING',
  DRAGGING = 'DRAGGING',
  STOPPING_AT_PORT = 'STOPPING_AT_PORT',
  UNKNOWN = 'UNKNOWN',
  CHARGING = 'CHARGING',
}

export const workStatusText = {
  [WorkStatus.DRAGGING]: '拖带/顶推',
  [WorkStatus.HOVERING]: '航行中',
  [WorkStatus.STOPPING_AT_PORT]: '停泊在港',
  [WorkStatus.UNKNOWN]: '未知',
  [WorkStatus.CHARGING]: '充电中',
};
