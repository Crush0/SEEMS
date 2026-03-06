<template>
  <div ref="containerRef" class="container">
    <Breadcrumb :items="['menu.dashboard', 'menu.dashboard.monitor']" />
    <a-row class="layout" :wrap="false" justify="center">
      <a-col class="layout-left-side" :span="4">
        <Battery style="margin-bottom: 16px" />
        <BatteryAlarms
          title="左电池簇报警"
          :alarms="realTimeData.leftBatteryAlarm"
        />
      </a-col>
      <a-col class="layout-content" :span="16">
        <a-space :size="16" direction="vertical" fill>
          <DataStatistic :real-time-data="realTimeData" :ship-info="shipInfo" />
        </a-space>
      </a-col>
      <a-col class="layout-right-side" :span="4">
        <a-space :size="16" direction="vertical" fill>
          <TimeBox :time="realTimeData.time" />
          <BatteryAlarms
            title="右电池簇报警"
            :alarms="realTimeData.rightBatteryAlarm"
          />
        </a-space>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { useShipDataStore } from '@/store';
  import { storeToRefs } from 'pinia';
  import { queryShipInfo } from '@/api/dashboard';
  import { onMounted, ref } from 'vue';
  import DataStatistic from './components/data-statistic.vue';
  import Battery from './components/battery.vue';
  import TimeBox from './components/time-box.vue';
  import BatteryAlarms from './components/battery-alarms.vue';

  const shipDataStore = useShipDataStore();
  const { realTimeData, shipInfo } = storeToRefs(shipDataStore);
  const fetchData = async () => {
    const { data } = await queryShipInfo();
    shipDataStore.updateShipInfo(data);
  };
  const containerRef = ref<HTMLDivElement | null>(null);
  fetchData();

  onMounted(() => {
    // containerRef.value.style.transform = 'scale(0.5)'
  });
</script>

<script lang="ts">
  export default {
    name: 'Monitor',
  };
</script>

<style lang="less" scoped>
  .container {
    padding: 0 4px;
  }
  .compass-container {
    width: 100%;
    height: 200px;
    margin: 0 auto;
  }
</style>
