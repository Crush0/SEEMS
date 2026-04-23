<template>
  <a-card
    class="general-card"
    :title="$t('dataAnalysis.title.publicOpinion')"
    :header-style="{ paddingBottom: '12px' }"
  >
    <template #extra>
      <span style="color: #999; font-size: 12px"
        >数据每小时更新一次 上次更新时间：{{ renderData.analyzeTime }}</span
      >
      <a-button
        type="text"
        :loading="loading"
        shape="circle"
        style="margin: 4px"
        @click="analyzeNow"
      >
        <template #icon>
          <icon-refresh />
        </template>
      </a-button>
    </template>
    <a-grid :cols="24" :col-gap="12" :row-gap="12">
      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <ChainItem
          :loading="loading"
          :title="$t('dataAnalysis.card.title.dailyEnergyConsumption')"
          :data="renderData.dailyEnergyConsumption"
          extra="d"
          chart-type="line"
          :card-style="{
            background: isDark
              ? 'linear-gradient(180deg, #284991 0%, #122B62 100%)'
              : 'linear-gradient(180deg, #f2f9fe 0%, #e6f4fe 100%)',
          }"
          ><template #suffix> kwh </template></ChainItem
        >
      </a-grid-item>
      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <ChainItem
          :loading="loading"
          :title="$t('dataAnalysis.card.title.hourlyEnergyConsumption')"
          extra="h"
          :data="renderData.hourlyEnergyConsumption"
          chart-type="line"
          :card-style="{
            background: isDark
              ? ' linear-gradient(180deg, #3D492E 0%, #263827 100%)'
              : 'linear-gradient(180deg, #F5FEF2 0%, #E6FEEE 100%)',
          }"
          ><template #suffix> kwh </template></ChainItem
        >
      </a-grid-item>
      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <ChainItem
          :loading="loading"
          :title="$t('dataAnalysis.card.title.unitWorkEnergyConsumption')"
          :data="renderData.unitTransportationEnergyConsumption"
          extra="d"
          chart-type="line"
          :card-style="{
            background: isDark
              ? 'linear-gradient(180deg, #294B94 0%, #0F275C 100%)'
              : 'linear-gradient(180deg, #f2f9fe 0%, #e6f4fe 100%)',
          }"
        >
          <template #suffix> kwh/(t·km) </template></ChainItem
        >
      </a-grid-item>
      <a-grid-item :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 6, xxl: 6 }">
        <ChainItem
          :loading="loading"
          :title="$t('dataAnalysis.card.title.unitDistanceEnergyConsumption')"
          extra="d"
          :data="renderData.unitDistanceEnergyConsumption"
          chart-type="line"
          :card-style="{
            background: isDark
              ? 'linear-gradient(180deg, #312565 0%, #201936 100%)'
              : 'linear-gradient(180deg, #F7F7FF 0%, #ECECFF 100%)',
          }"
        >
          <template #suffix> kwh/km </template></ChainItem
        >
      </a-grid-item>
    </a-grid>
  </a-card>
</template>

<script lang="ts" setup>
  import useThemes from '@/hooks/themes';
  import { ref } from 'vue';
  import { analyzeImmediately, queryDataAnalysis } from '@/api/visualization';
  import useLoading from '@/hooks/loading';
  import { Message } from '@arco-design/web-vue';
  import ChainItem from './chain-item.vue';

  const { loading, setLoading } = useLoading(true);
  const { isDark } = useThemes();

  const renderData = ref({
    analyzeTime: '',
    dailyEnergyConsumption: [],
    hourlyEnergyConsumption: [],
    unitTransportationEnergyConsumption: [],
    unitDistanceEnergyConsumption: [],
  });
  const fetchData = async () => {
    const { data: analysisDataList } = await queryDataAnalysis();
    if (analysisDataList.length > 0) {
      renderData.value.analyzeTime =
        analysisDataList[analysisDataList.length - 1].analyzeTime;
      renderData.value.dailyEnergyConsumption = analysisDataList.map(
        (item) => ({
          num: item.dailyEnergyConsumption,
          time: item.analyzeTime,
        })
      );
      renderData.value.hourlyEnergyConsumption = analysisDataList.map(
        (item) => ({
          num: item.preHourEnergyConsumption,
          time: item.analyzeTime,
        })
      );
      renderData.value.unitTransportationEnergyConsumption =
        analysisDataList.map((item) => ({
          num: item.preUnitWorkEnergyConsumption,
          time: item.analyzeTime,
        }));
      renderData.value.unitDistanceEnergyConsumption = analysisDataList.map(
        (item) => ({
          num: item.preDistanceEnergyConsumption,
          time: item.analyzeTime,
        })
      );
    }
    setLoading(false);
  };
  const analyzeNow = async () => {
    setLoading(true);
    await analyzeImmediately();
    await fetchData();
    Message.success('数据分析成功');
    setLoading(false);
  };

  fetchData();
</script>
