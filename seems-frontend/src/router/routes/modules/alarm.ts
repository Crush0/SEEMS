import { DEFAULT_LAYOUT } from '../base';
export default {
  path: '/alarm',
  name: 'alarm',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.alarm',
    requiresAuth: true,
    icon: 'icon-notification',
    order: 5,
  },
  children: [
    {
      path: '/alarm/strategy',
      name: 'alarm-strategy',
      component: () => import('@/views/alarm/strategy/index.vue'),
      meta: {
        locale: 'menu.alarm.strategy',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};
