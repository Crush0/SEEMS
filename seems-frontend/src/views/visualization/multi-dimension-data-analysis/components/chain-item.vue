<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :title="title"
      :header-style="{ paddingBottom: '12px' }"
    >
      <div class="content">
        <a-statistic
          :value="count"
          :show-group-separator="true"
          :value-from="0"
          animation
        />
        <a-typography-text
          class="percent-text"
          :type="isUp ? 'danger' : 'success'"
        >
          {{ growth }}%
          <icon-arrow-rise v-if="isUp" />
          <icon-arrow-fall v-else />
        </a-typography-text>
      </div>
      <div class="chart">
        <Chart :option="chartOption" />
      </div>
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  // import { queryDataChainGrowth, DataChainGrowth } from '@/api/visualization';
  import useChartOption from '@/hooks/chart-option';

  const props = defineProps({
    title: {
      type: String,
      default: '',
    },
    loading: {
      type: Boolean,
      default: false,
    },
    data: {
      type: Object as () => any,
      required: true,
    },
    chartType: {
      type: String,
      default: '',
    },
  });
  const count = ref(0);
  const growth = ref(100);
  watch(
    () => props.data,
    ({ data, list }) => {
      count.value = Number.parseFloat(data ?? '0.0');
    },
    {
      deep: true,
      immediate: true,
    }
  );
  const isUp = computed(() => growth.value > 50);
  const chartData = ref<any>([]);
  const { chartOption } = useChartOption(() => {
    return {
      grid: {
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
      },
      xAxis: {
        type: 'category',
        show: false,
      },
      yAxis: {
        show: false,
      },
      tooltip: {
        show: true,
        trigger: 'axis',
        formatter: '{c}',
      },
      series: [
        {
          data: chartData.value,
          ...(props.chartType === 'bar'
            ? {
                type: 'bar',
                barWidth: 7,
                barGap: '0',
              }
            : {
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                  color: '#4080FF',
                },
              }),
        },
      ],
    };
  });
</script>

<style scoped lang="less">
  .general-card {
    min-height: 204px;
  }
  .content {
    display: flex;
    align-items: center;
    width: 100%;
    margin-bottom: 12px;
  }
  .percent-text {
    margin-left: 16px;
  }
  .chart {
    width: 100%;
    height: 80px;
    vertical-align: bottom;
  }

  .unit {
    padding-left: 8px;
    font-size: 12px;
  }

  .label {
    padding-right: 8px;
    font-size: 12px;
  }
</style>
