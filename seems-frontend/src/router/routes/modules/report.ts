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
    {
      path: 'tug-monthly-report',
      name: 'TugMonthlyReport',
      component: () => import('@/views/report/tug-monthly-report/index.vue'),
      meta: {
        locale: 'menu.report.tugMonthlyReport',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default REPORT;
