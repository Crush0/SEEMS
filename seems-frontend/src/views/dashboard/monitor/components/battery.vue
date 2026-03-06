<template>
  <a-card title="剩余电量">
    <div class="battery-box">
      <a-statistic
        :value="
          (realTimeData.leftBatteryCapacity +
            realTimeData.rightBatteryCapacity) /
          2
        "
        :precision="2"
      >
        <template #suffix> % </template>
      </a-statistic>
    </div>
  </a-card>
</template>

<script setup>
  import { useShipDataStore } from '@/store';
  import { computed } from 'vue';

  const shipDataStore = useShipDataStore();
  const realTimeData = computed(() => shipDataStore.getRealTimeData);
</script>

<style scoped>
  .battery-box {
    display: flex;
    justify-content: center;
    align-items: center;
  }
</style>
