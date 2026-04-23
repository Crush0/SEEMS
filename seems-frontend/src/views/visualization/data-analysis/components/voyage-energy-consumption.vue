<template>
  <a-card
    class="general-card"
    title="航次能效统计"
    :header-style="{ paddingBottom: '12px' }"
    :loading="loading"
  >
    <template #extra>
      <a-range-picker
        v-model="dateRange"
        :time-picker-props="{ defaultValue: '00:00:00' }"
        style="width: 380px"
        @change="fetchData"
      />
      <a-button
        type="text"
        :loading="loading"
        shape="circle"
        style="margin: 4px"
        @click="fetchData"
      >
        <template #icon>
          <icon-refresh />
        </template>
      </a-button>
    </template>

    <a-grid :cols="24" :col-gap="12" :row-gap="12">
      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <a-card :bordered="false" :style="cardStyle1">
          <a-statistic
            title="总航次数"
            :value="statistics.totalVoyages || 0"
            :value-from="0"
            :precision="0"
            animation
            show-group-separator
          >
            <template #suffix>
              <span class="unit-text">次</span>
            </template>
          </a-statistic>
        </a-card>
      </a-grid-item>

      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <a-card :bordered="false" :style="cardStyle2">
          <a-statistic
            title="总航程"
            :value="statistics.totalDistance || 0"
            :value-from="0"
            :precision="2"
            animation
            show-group-separator
          >
            <template #suffix>
              <span class="unit-text">km</span>
            </template>
          </a-statistic>
        </a-card>
      </a-grid-item>

      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <a-card :bordered="false" :style="cardStyle3">
          <a-statistic
            title="总耗电量"
            :value="statistics.totalPowerConsumption || 0"
            :value-from="0"
            :precision="2"
            animation
            show-group-separator
          >
            <template #suffix>
              <span class="unit-text">kWh</span>
            </template>
          </a-statistic>
        </a-card>
      </a-grid-item>

      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <a-card :bordered="false" :style="cardStyle4">
          <a-statistic
            title="平均航次耗电量"
            :value="statistics.averagePowerConsumption || 0"
            :value-from="0"
            :precision="2"
            animation
            show-group-separator
          >
            <template #suffix>
              <span class="unit-text">kWh/次</span>
            </template>
          </a-statistic>
        </a-card>
      </a-grid-item>
    </a-grid>

    <a-divider />

    <a-grid :cols="24" :col-gap="12" :row-gap="12">
      <a-grid-item :span="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 12, xxl: 12 }">
        <a-card :bordered="false" :style="cardStyle5">
          <a-statistic
            title="总航行时间"
            :value="formatTime(statistics.totalTime || 0)"
          >
            <template #suffix>
              <span class="unit-text">小时</span>
            </template>
          </a-statistic>
        </a-card>
      </a-grid-item>

      <a-grid-item :span="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 12, xxl: 12 }">
        <a-card :bordered="false" :style="cardStyle6">
          <a-statistic
            title="平均航程"
            :value="statistics.averageDistance || 0"
            :value-from="0"
            :precision="2"
            animation
            show-group-separator
          >
            <template #suffix>
              <span class="unit-text">km/次</span>
            </template>
          </a-statistic>
        </a-card>
      </a-grid-item>
    </a-grid>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref, onMounted, computed } from 'vue';
  import { getVoyageStatistics, type VoyageStatistics } from '@/api/voyage';
  import useLoading from '@/hooks/loading';
  import moment from 'moment';

  const { loading, setLoading } = useLoading(true);
  const dateRange = ref<[string, string]>([
    moment().subtract(30, 'day').format('YYYY-MM-DD HH:mm:ss'),
    moment().format('YYYY-MM-DD HH:mm:ss'),
  ]);

  const statistics = ref<VoyageStatistics>({
    totalVoyages: 0,
    totalDistance: 0,
    totalPowerConsumption: 0,
    totalTime: 0,
    averageDistance: 0,
    averagePowerConsumption: 0,
    averageTime: 0,
  });

  const cardStyle1 = computed(() => ({
    background: 'linear-gradient(180deg, #f2f9fe 0%, #e6f4fe 100%)',
  }));

  const cardStyle2 = computed(() => ({
    background: 'linear-gradient(180deg, #F5FEF2 0%, #E6FEEE 100%)',
  }));

  const cardStyle3 = computed(() => ({
    background: 'linear-gradient(180deg, #f2f9fe 0%, #e6f4fe 100%)',
  }));

  const cardStyle4 = computed(() => ({
    background: 'linear-gradient(180deg, #F7F7FF 0%, #ECECFF 100%)',
  }));

  const cardStyle5 = computed(() => ({
    background: 'linear-gradient(180deg, #fff7e6 0%, #ffe7ba 100%)',
  }));

  const cardStyle6 = computed(() => ({
    background: 'linear-gradient(180deg, #fff0f0 0%, #ffd6d6 100%)',
  }));

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return hours + (minutes > 0 ? '.' + Math.floor(minutes / 6) : '');
  };

  const fetchData = async () => {
    setLoading(true);
    try {
      const [start, end] = dateRange.value;
      const { data } = await getVoyageStatistics({
        startTime: start,
        endTime: end,
      });
      statistics.value = data;
    } catch (error) {
      console.error('获取航次统计数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  onMounted(() => {
    fetchData();
  });
</script>

<style scoped lang="less">
  .unit-text {
    font-size: 14px;
    color: #999;
    margin-left: 4px;
  }

  :deep(.arco-card) {
    border-radius: 4px;
  }

  :deep(.arco-card-body) {
    padding: 16px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 14px;
      color: #4e5969;
      margin-bottom: 8px;
    }

    .arco-statistic-content {
      font-size: 24px;
      font-weight: bold;
      color: #1d2129;
    }

    .arco-statistic-value {
      font-family: 'SF Pro', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
        'Helvetica Neue', Arial, sans-serif;
    }
  }
</style>
