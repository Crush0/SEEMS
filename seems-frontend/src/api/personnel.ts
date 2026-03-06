import axios from 'axios';

export interface PersonnelRecord {
  id?: string;
  name: string;
  email: string;
  phone: string;
  role: '管理员' | '操作员' | '普通用户';
  status: '正常' | '待审核' | '禁用';
}
export interface PersonnelListResp {
  list: PersonnelRecord[];
  total: number;
}

export interface queryPersonnelForm extends Partial<PersonnelRecord> {
  current: number;
  pageSize: number;
}

export function queryPersonnel(data: queryPersonnelForm) {
  return axios.post<PersonnelListResp>('/api/personnel/query', data);
}

export function savePersonnel(data: PersonnelRecord) {
  return axios.post<PersonnelRecord>('/api/personnel/save', data);
}
