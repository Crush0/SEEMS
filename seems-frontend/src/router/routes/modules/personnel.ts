import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const PERSONNEL: AppRouteRecordRaw = {
  path: '/personnel',
  name: 'Personnel',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.personnel',
    requiresAuth: true,
    icon: 'icon-user-group',
    order: 2,
  },
  children: [
    {
      path: 'personnel-search-table', // The midline path complies with SEO specifications
      name: 'PersonnelSearchTable',
      component: () => import('@/views/personnel/search-table/index.vue'),
      meta: {
        locale: 'menu.personnel.searchTable',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
  ],
};

export default PERSONNEL;
