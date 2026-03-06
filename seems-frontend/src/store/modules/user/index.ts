import { defineStore } from 'pinia';
import {
  getUserInfo,
  login as userLogin,
  LoginData,
  logout as userLogout,
  updateBasicUserInfo,
} from '@/api/user';
import { clearToken, setToken } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';
import { UserState } from './types';
import useAppStore from '../app';

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    username: undefined,
    avatar: undefined,
    job: undefined,
    organization: undefined,
    location: undefined,
    email: undefined,
    introduction: undefined,
    personalWebsite: undefined,
    jobName: undefined,
    organizationName: undefined,
    locationName: undefined,
    phone: undefined,
    registrationDate: undefined,
    accountId: undefined,
    certification: undefined,
    role: '',
    status: '',
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
  },

  actions: {
    // Set user's information
    setInfo(partial: { user: Partial<UserState>; role: string }) {
      this.$patch({
        ...partial.user,
        role: partial.role,
      });
      // console.log(this.$state);
    },

    // Reset user's information
    resetInfo() {
      this.$reset();
    },

    // Get user's information
    async info() {
      const res = await getUserInfo();
      const PERMISSION = {
        管理员: 'admin',
        普通用户: 'user',
        操作员: 'operator',
      };
      this.setInfo({
        ...res.data,
        role: PERMISSION[(res.data as any).user.role],
      });
    },

    async updateBasicInfo(params: Partial<{ email; phone }>) {
      const res = await updateBasicUserInfo({
        email: params.email === '' ? undefined : params.email,
        phone: params.phone === '' ? undefined : params.phone,
      });
      // @ts-ignore
      if (res.code === 20000) {
        this.info();
        return Promise.resolve();
      }
      return Promise.reject();
    },

    // Login
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm);
        setToken(res.data.token);
      } catch (err) {
        clearToken();
        throw err;
      }
    },
    logoutCallBack() {
      const appStore = useAppStore();
      this.resetInfo();
      clearToken();
      removeRouteListener();
      appStore.clearServerMenu();
    },
    // Logout
    async logout() {
      try {
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
  },
});

export default useUserStore;
