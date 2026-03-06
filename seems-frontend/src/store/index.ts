import { createPinia } from 'pinia';
import useAppStore from './modules/app';
import useUserStore from './modules/user';
import useTabBarStore from './modules/tab-bar';
import useShipDataStore from './modules/ship-data';

const pinia = createPinia();

export { useAppStore, useUserStore, useTabBarStore, useShipDataStore };
export default pinia;
