<template>
  <div class="container">
    <Breadcrumb
      :items="[
        'menu.visualization',
        'menu.visualization.multiDimensionDataAnalysis',
      ]"
    />
    <a-space direction="vertical" :size="16" fill>
      <a-grid :cols="24" :col-gap="16" :row-gap="16">
        <a-grid-item
          :span="{ xs: 24, sm: 24, md: 24, lg: 18, xl: 18, xxl: 18 }"
        >
          <DataOverview
            :loading="loading"
            :data="renderData"
            :list="dataList"
            @fetch-data="fetchData"
          />
        </a-grid-item>
        <a-grid-item :span="{ xs: 24, sm: 24, md: 24, lg: 6, xl: 6, xxl: 6 }">
          <UserActions style="margin-bottom: 16px" />
          <CIIRate :loading="loading" :data="renderData" />
        </a-grid-item>
      </a-grid>
      <DataChainGrowth :list="dataList" :loading="loading" :data="renderData" />
      <ContentPublishingSource />
    </a-space>
  </div>
</template>

<script lang="ts" setup>
  import { queryHistoryReport, ReportParams } from '@/api/report';
  import useLoading from '@/hooks/loading';
  import { ref } from 'vue';
  import DataOverview from './components/data-overview.vue';
  import DataChainGrowth from './components/data-chain-growth.vue';
  import UserActions from './components/user-actions.vue';
  import CIIRate from './components/cii-rate.vue';
  import ContentPublishingSource from './components/content-publishing-source.vue';

  const { loading, setLoading } = useLoading(false);

  const renderData = ref({} as any);

  const dataList = ref([] as any[]);

  const fetchData = (data: ReportParams) => {
    setLoading(true);
    queryHistoryReport(data)
      .then((res) => {
        renderData.value =
          res.data.analyzeData[res.data.analyzeData.length - 1];
        dataList.value = res.data.analyzeData;
      })
      .finally(() => {
        setLoading(false);
      });
  };
</script>

<script lang="ts">
  export default {
    name: 'MultiDimensionDataAnalysis',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }

  :deep(.section-title) {
    margin-top: 0;
    margin-bottom: 16px;
    font-size: 16px;
  }

  :deep(.chart-wrap) {
    height: 264px;
  }
</style>
