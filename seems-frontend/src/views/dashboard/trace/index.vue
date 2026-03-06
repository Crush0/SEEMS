<template>
  <div class="trace-view">
    <a-card
      class="trace-form"
      :body-style="{
        padding: '0px',
      }"
      @click.stop
    >
      <a-form :model="traceForm" size="small" layout="inline">
        <a-space fill>
          <a-form-item>
            <a-range-picker
              v-model:model-value="traceForm.timeRange"
              style="margin-top: 6px"
              show-time
              format="YYYY-MM-DD HH:mm"
              :disabled="loading"
            />
          </a-form-item>
          <a-form-item no-style>
            <a-switch v-model="traceForm.showMarker" :disabled="loading">
              <template #checked>显示标志</template>
              <template #unchecked>隐藏标志</template>
            </a-switch>
          </a-form-item>
          <a-form-item no-style>
            <a-switch v-model="traceForm.showTrace" :disabled="loading">
              <template #checked>显示轨迹</template>
              <template #unchecked>隐藏轨迹</template>
            </a-switch>
          </a-form-item>
        </a-space>
      </a-form>
      <!--      <div style="margin-top: 10px; padding: 15px" class="ship-info-box">-->

      <!--      </div>-->
    </a-card>
    <a-spin :loading="loading" tip="加载中..." style="width: 100%">
      <div class="map-container">
        <MapContainer
          :map-container-style="{
            padding: '0px',
            margin: '0px',
            width: '100%',
            height: 'calc(100vh - 110px)',
          }"
          :position-data="positionData"
          :ship-location="shipLocation"
          :ship-direction="shipDirection"
          :show-marker="traceForm.showMarker"
          :show-trace="traceForm.showTrace"
          :center="[shipLocation.longitude, shipLocation.latitude]"
        />
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import MapContainer from '@/components/mapContainer/index.vue';
  import { queryShipNavigationDataByDateTimeBetween } from '@/api/dashboard';
  import { onMounted, ref, watch } from 'vue';
  import moment from 'moment';
  import { Message } from '@arco-design/web-vue';
  import { Position } from '@/store/modules/ship-data/type';
  import useLoading from '@/hooks/loading';

  const traceForm = ref({
    timeRange: [
      new Date(new Date().setSeconds(0)).setMinutes(0),
      new Date(new Date().setSeconds(59)).setMinutes(59),
    ],
    showMarker: true,
    showTrace: true,
  });
  const positionData = ref([]);
  const shipLocation = ref({
    latitude: 34.752206,
    longitude: 119.420108,
  } as Position);
  const shipDirection = ref(0);
  const { loading, setLoading } = useLoading(false);
  async function getPositionData(startDate: string, endDate: string) {
    setLoading(true);
    const { data } = await queryShipNavigationDataByDateTimeBetween({
      start: startDate,
      end: endDate,
    });
    const position = data.navData.map((item) => [
      item.latitude,
      item.longitude,
    ]);
    if (position.length === 0) {
      Message.warning('轨迹数据为空，请重新选择时间范围');
      traceForm.value.showMarker = false;
      traceForm.value.showTrace = false;
      setLoading(false);
    }
    shipLocation.value = {
      latitude: position[position.length - 1][0],
      longitude: position[position.length - 1][1],
    };
    shipDirection.value = data.navData[position.length - 1].direction;

    setLoading(false);
    return position;
  }

  watch(
    () => traceForm.value.timeRange,
    async () => {
      positionData.value = await getPositionData(
        moment(traceForm.value.timeRange[0]).format('YYYY-MM-DD HH:mm:ss'),
        moment(traceForm.value.timeRange[1]).format('YYYY-MM-DD HH:mm:ss')
      );
    },
    {
      deep: true,
      immediate: true,
    }
  );
  onMounted(() => {
    // getPositionData(
    //   moment(timeRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
    //   moment(timeRange.value[1]).format('YYYY-MM-DD HH:mm:ss')
    // );
  });
</script>

<style scoped>
  .trace-view {
    position: relative;
  }

  .trace-form {
    position: absolute;
    top: 10px;
    right: 20px;
    z-index: 10;
    width: 600px;
    padding: 5px;
    background-color: rgb(0 0 0 / 30%);
    border-radius: 4px;
    box-shadow: 0 0 10px rgb(0 0 0 / 30%);
  }
</style>
