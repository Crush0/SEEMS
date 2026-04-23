import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const MESSAGE: AppRouteRecordRaw = {
  path: '/message',
  name: 'Message',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.message',
    requiresAuth: true,
    icon: 'icon-message',
    order: 3,
  },
  children: [
    {
      path: 'index',
      name: 'MessageIndex',
      component: () => import('@/views/message/ocean-design.vue'),
      meta: {
        locale: 'menu.message.index',
        requiresAuth: true,
      },
    },
  ],
};

export default MESSAGE;
