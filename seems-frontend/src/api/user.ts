import axios from 'axios';
import type { RouteRecordNormalized } from 'vue-router';
import { UserState } from '@/store/modules/user/types';

export interface LoginData {
  username: string;
  password: string;
  rememberMe: boolean;
}

export interface RegisterData {
  username: string;
  password: string;
  repeatPassword: string;
}

export interface LoginRes {
  token: string;
}
export function login(data: LoginData) {
  return axios.post<LoginRes>('/api/user/login', data);
}

export function register(data: RegisterData) {
  return axios.post('/api/user/register', data);
}

export function resetPassword(username: string) {
  return axios.post('/api/user/reset-password', { username });
}

export function logout() {
  return axios.post<LoginRes>('/api/user/logout');
}

export function getUserInfo() {
  return axios.post<UserState>('/api/user/info');
}

export function getMenuList() {
  return axios.post<RouteRecordNormalized[]>('/api/user/menu');
}

export function updateBasicUserInfo(data: Partial<{ email; phone }>) {
  return axios.post<any>('/api/user/update-basic-info', data);
}

export function updateAvatar(data: string) {
  return axios.post('/api/user/update-avatar', {
    avatar: data,
  });
}

export function editPassword(data: {
  oldPassword;
  newPassword;
  repeatPassword;
}) {
  return axios.post('/api/user/edit-password', {
    ...data,
  });
}
