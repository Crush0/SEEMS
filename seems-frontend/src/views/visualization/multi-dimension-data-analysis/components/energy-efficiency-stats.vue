<template>
  <a-card class="general-card" title="能效统计概览">
    <a-spin :loading="loading" style="width: 100%">
      <a-row :gutter="[8, 8]">
        <a-col :span="12">
          <a-statistic
            title="日耗电量"
            :value="energyStats.dailyEnergyConsumption"
            :precision="2"
            :value-from="0"
            animation
          >
            <template #suffix>
              <span class="unit-text">kWh</span>
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="12">
          <a-statistic
            title="小时耗电量"
            :value="energyStats.hourlyEnergyConsumption"
            :precision="2"
            :value-from="0"
            animation
          >
            <template #suffix>
              <span class="unit-text">kWh</span>
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="12">
          <a-statistic
            title="单位运输功能耗"
            :value="energyStats.unitWorkEnergyConsumption"
            :precision="4"
            :value-from="0"
            animation
          >
            <template #suffix>
              <span class="unit-text">kWh/(t·km)</span>
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="12">
          <a-statistic
            title="单位距离能耗"
            :value="energyStats.unitDistanceEnergyConsumption"
            :precision="4"
            :value-from="0"
            animation
          >
            <template #suffix>
              <span class="unit-text">kWh/km</span>
            </template>
          </a-statistic>
        </a-col>
      </a-row>
    </a-spin>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { getVoyageStatistics } from '@/api/voyage';
  import useLoading from '@/hooks/loading';
  import moment from 'moment';

  const { loading, setLoading } = useLoading(true);

  const energyStats = ref({
    dailyEnergyConsumption: 0,
    hourlyEnergyConsumption: 0,
    unitWorkEnergyConsumption: 0,
    unitDistanceEnergyConsumption: 0,
  });

  const fetchEnergyStats = async () => {
    setLoading(true);
    try {
      // 获取今日统计数据
      const { data } = await getVoyageStatistics({
        startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: moment().format('YYYY-MM-DD HH:mm:ss'),
      });

      // 计算各项指标（使用示例数据，实际应从后端获取）
      if (data.totalVoyages > 0) {
        energyStats.value = {
          dailyEnergyConsumption: data.totalPowerConsumption || 0,
          hourlyEnergyConsumption: data.averagePowerConsumption || 0,
          unitWorkEnergyConsumption: data.averagePowerConsumption / (data.averageDistance || 1) * 0.1 || 0,
          unitDistanceEnergyConsumption: data.averagePowerConsumption / (data.averageDistance || 1) || 0,
        };
      }
    } catch (error) {
      console.error('获取能效统计数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  onMounted(() => {
    fetchEnergyStats();
  });
</script>

<style scoped lang="less">
  .unit-text {
    font-size: 12px;
    color: #999;
    margin-left: 4px;
  }

  :deep(.arco-card-body) {
    padding: 16px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 12px;
      color: #86909c;
      margin-bottom: 4px;
    }

    .arco-statistic-content {
      font-size: 16px;
      font-weight: 500;
    }
  }
</style>
