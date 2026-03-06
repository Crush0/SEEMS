<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card :bordered="false" :style="cardStyle">
      <div class="content-wrap">
        <div class="content">
          <a-statistic
            :title="title"
            :value="Number.parseFloat(count)"
            :value-from="0"
            :precision="2"
            animation
            show-group-separator
          >
            <template #suffix>
              <slot name="suffix"></slot>
            </template>
          </a-statistic>
          <div class="desc">
            <a-typography-text type="secondary" class="label">
              {{ extra === 'h' ? '较上个小时' : '较昨日' }}
            </a-typography-text>
            <a-typography-text
              type="danger"
              style="width: 100%"
              :style="{ color: growth > 0 ? 'red' : 'green' }"
            >
              {{ Math.abs(growth).toFixed(2) }}
              <icon-arrow-rise v-if="growth > 0" />
              <icon-arrow-fall v-else />
            </a-typography-text>
          </div>
        </div>
        <div class="chart">
          <Chart v-if="false" :option="chartOption" />
        </div>
      </div>
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, CSSProperties, onMounted, PropType, ref } from 'vue';
  import useChartOption from '@/hooks/chart-option';

  // const barChartOptionsFactory = () => {
  //   const data = ref<any>([]);
  //   const { chartOption } = useChartOption(() => {
  //     return {
  //       grid: {
  //         left: 0,
  //         right: 0,
  //         top: 10,
  //         bottom: 0,
  //       },
  //       xAxis: {
  //         type: 'category',
  //         show: false,
  //       },
  //       yAxis: {
  //         show: false,
  //       },
  //       tooltip: {
  //         show: true,
  //         trigger: 'axis',
  //       },
  //       series: {
  //         name: 'total',
  //         data,
  //         type: 'bar',
  //         barWidth: 7,
  //         itemStyle: {
  //           borderRadius: 2,
  //         },
  //       },
  //     } as any;
  //   });
  //   return {
  //     data,
  //     chartOption,
  //   } as any;
  // };
  const props = defineProps({
    loading: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: '',
    },
    extra: {
      type: String,
      default: '',
    },
    data: {
      type: Object as PropType<
        {
          time: string;
          num: number;
        }[]
      >,
      // eslint-disable-next-line vue/require-valid-default-prop
      default: () => [],
    },
    chartType: {
      type: String,
      default: '',
    },
    cardStyle: {
      type: Object as PropType<CSSProperties>,
      default: () => {
        return {};
      },
    },
  });
  const lineChartOptionsFactory = () => {
    const data = ref<number[]>([]);
    const { chartOption } = useChartOption(() => {
      return {
        grid: {
          left: 0,
          right: 0,
          top: 10,
          bottom: 10,
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
        },
        series: [
          {
            name: props.title,
            data: data.value,
            type: 'line',
            showSymbol: false,
            smooth: true,
            lineStyle: {
              color: '#165DFF',
              width: 3,
            },
          },
        ],
      };
    });
    return {
      data,
      chartOption,
    };
  };

  // const pieChartOptionsFactory = () => {
  //   const data = ref<any>([]);
  //   const { chartOption } = useChartOption(() => {
  //     return {
  //       grid: {
  //         left: 0,
  //         right: 0,
  //         top: 0,
  //         bottom: 0,
  //       },
  //       legend: {
  //         show: true,
  //         top: 'center',
  //         right: '0',
  //         orient: 'vertical',
  //         icon: 'circle',
  //         itemWidth: 6,
  //         itemHeight: 6,
  //         textStyle: {
  //           color: '#4E5969',
  //         },
  //       },
  //       tooltip: {
  //         show: true,
  //       },
  //       series: [
  //         {
  //           name: '总计',
  //           type: 'pie',
  //           radius: ['50%', '70%'],
  //           label: {
  //             show: false,
  //           },
  //           data,
  //         },
  //       ],
  //     } as any;
  //   });
  //   return {
  //     data,
  //     chartOption,
  //   };
  // };

  const { chartOption: lineChartOption } = lineChartOptionsFactory();

  const chartOption = ref({});
  const count = computed(() => {
    if (props.data.length === 0) {
      return 0;
    }

    return props.data[props.data.length - 1]
      ? props.data[props.data.length - 1].num ?? 0
      : 0;
  });

  // eslint-disable-next-line consistent-return,vue/return-in-computed-property
  const growth = computed(() => {
    if (props.data.length < 2) {
      return 0;
    }
    const lastAnalyzeTime = new Date(props.data[props.data.length - 1].time);
    if (props.extra === 'h') {
      // 较上小时
      // 找到最近一条时间比最后一条记录早1h的记录
      const oneHourAgo = new Date(lastAnalyzeTime);
      oneHourAgo.setHours(oneHourAgo.getHours() - 1);
      let latestRecord = null;
      for (let i = props.data.length - 2; i >= 0; i -= 1) {
        const recordTime = new Date(props.data[i].time);
        if (recordTime <= lastAnalyzeTime && recordTime >= oneHourAgo) {
          latestRecord = props.data[i];
          break;
        }
      }
      if (latestRecord) {
        return count.value - latestRecord.num;
      }
      return 0;
    }
    if (props.extra === 'd') {
      // 较昨日
      // 找到最近一条时间比最后一条记录早24h的记录
      const previousDay = new Date(lastAnalyzeTime);
      previousDay.setDate(previousDay.getDate() - 1);
      const previousDayString = previousDay.toISOString().split('T')[0];
      let latestYesterdayRecord = null;
      for (let i = props.data.length - 1; i >= 0; i -= 1) {
        const recordDate = props.data[i].time.split(' ')[0];
        if (recordDate === previousDayString) {
          latestYesterdayRecord = props.data[i];
          break;
        }
      }
      if (latestYesterdayRecord) {
        return count.value - latestYesterdayRecord.num;
      }
      return 0;
    }
  });

  // watch(
  //   () => props.data,
  //   (newVal) => {
  //     if (newVal.length === 0) {
  //       return;
  //     }
  //     lineData.value = newVal.map((item) => item.num)
  //   },{
  //     deep: true,
  //     immediate: true,
  //   }
  // );

  onMounted(() => {
    chartOption.value = lineChartOption.value;
  });
</script>

<style scoped lang="less">
  .desc {
    width: 150px;
  }
  :deep(.arco-card) {
    border-radius: 4px;
  }
  :deep(.arco-card-body) {
    width: 100%;
    height: 134px;
    padding: 0;
  }
  .content-wrap {
    width: 100%;
    padding: 16px;
    white-space: nowrap;
  }
  :deep(.content) {
    float: left;
    width: 108px;
    height: 102px;
  }
  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 16px;
      font-weight: bold;
      white-space: nowrap;
    }
    .arco-statistic-content {
      margin-top: 10px;
    }
  }

  .chart {
    float: right;
    width: calc(100% - 108px);
    height: 90px;
    vertical-align: bottom;
  }

  .label {
    padding-right: 8px;
    font-size: 12px;
  }
</style>
