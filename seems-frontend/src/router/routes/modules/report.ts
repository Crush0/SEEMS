import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const REPORT: AppRouteRecordRaw = {
  path: '/report',
  name: 'Report',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.report',
    requiresAuth: true,
    icon: 'icon-file',
    order: 1,
  },
  children: [
    {
      path: 'noon-report',
      name: 'NoonReport',
      component: () => import('@/views/report/noon-report/index.vue'),
      meta: {
        locale: 'menu.visualization.noonReport',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default REPORT;
