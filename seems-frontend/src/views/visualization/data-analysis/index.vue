<template>
  <div class="container">
    <Breadcrumb
      :items="['menu.visualization', 'menu.visualization.dataAnalysis']"
    />
    <a-space direction="vertical" :size="12" fill>
      <a-space direction="vertical" :size="16" fill>
        <div class="space-unit">
          <CompusitionData />
        </div>
        <div>
          <a-grid :cols="24" :col-gap="16" :row-gap="16">
            <a-grid-item
              :span="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 16, xxl: 16 }"
            >
              <NavigationChart />
            </a-grid-item>
            <a-grid-item
              :span="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 8, xxl: 8 }"
            >
              <WorkingConditionPercent />
            </a-grid-item>
          </a-grid>
        </div>
        <div>
          <!-- <ContentPeriodAnalysis /> -->
        </div>
      </a-space>
    </a-space>
  </div>
</template>

<script lang="ts" setup>
  import CompusitionData from './components/compusition-data.vue';
  import NavigationChart from './components/navigation-chart.vue';
  import WorkingConditionPercent from './components/working-condition-percent.vue';
</script>

<script lang="ts">
  export default {
    name: 'DataAnalysis',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
    margin-bottom: 20px;
  }

  .space-unit {
    background-color: var(--color-bg-2);
    border-radius: 4px;
  }

  .title-fix {
    margin: 0 0 12px 0;
    font-size: 14;
  }
  :deep(.section-title) {
    margin: 0 0 12px 0;
    font-size: 14px;
  }
</style>
