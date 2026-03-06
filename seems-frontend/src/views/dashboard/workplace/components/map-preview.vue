<template>
  <a-card
    class="general-card"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '0', position: 'relative' }"
  >
    <div class="detail-btn-box">
      <a-button
        type="text"
        size="small"
        class="detail-btn"
        @click="$router.push({ name: 'Trace' })"
        >详细轨迹</a-button
      >
    </div>
    <MapContainer
      :map-container-style="{
        padding: '0px',
        margin: '0px',
        width: '100%',
        height: '200px',
      }"
      :position-data="positionData"
      :ship-location="shipLocation"
      :ship-direction="shipDirection"
      :center="[shipLocation.longitude, shipLocation.latitude]"
    />
  </a-card>
</template>

<script lang="ts" setup>
  import MapContainer from '@/components/mapContainer/index.vue';
  import { useShipDataStore } from '@/store';
  import { computed } from 'vue';

  const shipDataStore = useShipDataStore();

  const positionData = computed(() => shipDataStore.getPositionData);
  const realTimeData = computed(() => shipDataStore.getRealTimeData);
  const shipLocation = computed(() => realTimeData.value.position);
  const shipDirection = computed(() => realTimeData.value.direction);
</script>

<style scoped lang="less">
  .detail-btn-box {
    position: absolute;
    top: 5px;
    right: 4px;
    z-index: 1;
  }
</style>
