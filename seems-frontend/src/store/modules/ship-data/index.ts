import { defineStore } from 'pinia';
import { ShipInfo } from '@/api/flow';
import {
  PropellerWorkStatus,
  RealTimeData,
  ShipDataRecord,
  ShipSocData,
} from './type';

const useShipDataStore = defineStore('shipData', {
  state: (): {
    navData: ShipDataRecord[];
    // speedData: ShipSpeedData[];
    shipInfo: ShipInfo;
    realTimeData: RealTimeData;
    socData: ShipSocData[];
  } => ({
    socData: [],
    navData: [],
    shipInfo: {} as ShipInfo,
    realTimeData: {
      time: Number.NaN,
      direction: 0,
      position: {
        latitude: 0,
        longitude: 0,
      },
      speed: 0,
      leftBatteryCapacity: 0,
      rightBatteryCapacity: 0,
      // 左推进器
      leftPropeller: {
        status: PropellerWorkStatus.UNKNOWN,
        rpm: 0,
        power: 0,
        degrees: 0,
      },
      // 右推进器
      rightPropeller: {
        status: PropellerWorkStatus.UNKNOWN,
        rpm: 0,
        power: 0,
        degrees: 0,
      },
      sailDuration: 0,
      sailRange: 0,
      powerDissipation: 0,
      leftBatteryAlarm: Array.from({ length: 16 }, () => false),
      rightBatteryAlarm: Array.from({ length: 16 }, () => false),
      totalSailRange: 0,
      windDirection: 0,
      windSpeed: 0,
    } as RealTimeData,
  }),
  getters: {
    getPositionData(state): number[][] {
      return state.navData.map((item) => [item.latitude, item.longitude]);
    },
    getShipInfo(state): ShipInfo {
      return state.shipInfo;
    },
    getRealTimeData(state): RealTimeData {
      return state.realTimeData;
    },
  },
  actions: {
    updateRealTimeData(partial: Partial<{ realTimeData: RealTimeData }>) {
      this.$patch({
        realTimeData: partial.realTimeData,
      });
      // console.log('updateRealTimeData', this.realTimeData);
    },
    updateNavData(partial: Partial<{ navData: ShipDataRecord[] }>) {
      // @ts-ignore-next-line
      this.$patch({
        navData: partial.navData,
      });
    },
    updateSocData(partial: Partial<{ socData: ShipSocData[] }>) {
      // @ts-ignore-next-line
      this.$patch({
        socData: partial.socData,
      });
    },
    updateShipInfo(partial: Partial<{ shipInfo: ShipInfo }>) {
      // @ts-ignore-next-line
      this.$patch({
        shipInfo: partial.shipInfo,
      });
    },
  },
});

export default useShipDataStore;
