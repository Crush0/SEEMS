import { AppRouteRecordRaw } from '../types';

const FLOW: AppRouteRecordRaw = {
  path: '/flow',
  name: 'Flow',
  component: () => import('@/views/flow/index.vue'),
  meta: {
    locale: 'menu.flow',
    requiresAuth: true,
    order: 0,
    roles: ['*'],
  },
};

export default FLOW;
