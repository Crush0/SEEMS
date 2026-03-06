<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :title="$t('multiDAnalysis.card.title.dataOverview')"
    >
      <template #extra>
        <a-space>
          <!--          {{ form }}-->
          <template v-if="form.reportType === 'daily'">
            <a-date-picker
              v-model:model-value="form.time"
              style="width: 200px"
            />
          </template>
          <template v-else-if="form.reportType === 'weekly'">
            <a-input-group>
              <a-select
                v-model:model-value="form.year"
                :style="{ width: '120px' }"
                placeholder="选择一年..."
              >
                <a-option
                  v-for="year in renderYear"
                  :key="year.value"
                  :value="year.value"
                  >{{ year.text }}年</a-option
                >
              </a-select>
              <a-select
                v-model:model-value="form.week"
                :style="{ width: '250px' }"
                placeholder="选择一周..."
              >
                <a-option
                  v-for="(week, idx) in renderWeek"
                  :key="idx"
                  :value="week.value"
                  >{{ week.text }}</a-option
                >
              </a-select>
            </a-input-group>
          </template>
          <template v-else-if="form.reportType === 'monthly'">
            <a-input-group>
              <a-select
                v-model:model-value="form.year"
                :style="{ width: '120px' }"
                placeholder="选择一年..."
              >
                <a-option
                  v-for="year in renderYear"
                  :key="year.value"
                  :value="year.value"
                  >{{ year.text }}年</a-option
                >
              </a-select>
              <a-select
                v-model:model-value="form.month"
                :style="{ width: '120px' }"
                placeholder="选择一个月..."
              >
                <a-option
                  v-for="(month, idx) in [
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                  ]"
                  :key="idx"
                  :value="month"
                  >{{ month }}月</a-option
                >
              </a-select>
            </a-input-group>
          </template>
          <template v-else-if="form.reportType === 'quarterly'">
            <a-input-group>
              <a-select
                v-model:model-value="form.year"
                :style="{ width: '120px' }"
                placeholder="选择一年..."
              >
                <a-option
                  v-for="year in renderYear"
                  :key="year.value"
                  :value="year.value"
                  >{{ year.text }}年</a-option
                >
              </a-select>
              <a-select
                v-model:model-value="form.quarter"
                :style="{ width: '120px' }"
                placeholder="选择一个季度..."
              >
                <a-option
                  v-for="(quarter, idx) in [1, 2, 3, 4]"
                  :key="idx"
                  :value="quarter"
                  >第{{ quarter }}季度</a-option
                >
              </a-select>
            </a-input-group>
          </template>
          <template v-else-if="form.reportType === 'yearly'">
            <a-select
              v-model:model-value="form.year"
              :style="{ width: '120px' }"
              placeholder="选择一年..."
            >
              <a-option
                v-for="year in renderYear"
                :key="year.value"
                :value="year.value"
                >{{ year.text }}年</a-option
              >
            </a-select>
          </template>
          <a-radio-group
            v-model:model-value="form.reportType"
            :on-change="handleReportTypeChange"
            type="button"
            default-value="daily"
          >
            <a-radio value="daily">日报</a-radio>
            <a-radio value="weekly">周报</a-radio>
            <a-radio value="monthly">月报</a-radio>
            <a-radio value="quarterly">季报</a-radio>
            <a-radio value="yearly">年报</a-radio>
          </a-radio-group>
        </a-space>
      </template>
      <a-row justify="space-between">
        <a-col v-for="(item, idx) in renderData" :key="idx" :span="6">
          <a-statistic
            :title="item.title"
            :value="item.value"
            :precision="item.precision"
            show-group-separator
            :value-from="0"
            animation
          >
            <template #prefix>
              <span
                class="statistic-prefix"
                :style="{ background: item.prefix.background }"
              >
                <component
                  :is="item.prefix.icon"
                  :style="{ color: item.prefix.iconColor }"
                />
              </span>
            </template>
            <template #suffix>
              <sub>{{ item.suffix }}</sub>
            </template>
          </a-statistic>
        </a-col>
      </a-row>
      <Chart style="height: 328px; margin-top: 20px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  // import { useI18n } from 'vue-i18n';
  import useThemes from '@/hooks/themes';
  import {
    getDateRange,
    getNowFormatDate,
    getNumOfWeeks,
    getWeekNumber,
  } from '@/utils/time-format';
  import { isEmptyString } from '@/utils/is';
  import moment from 'moment';
  import { ReportParams } from '@/api/report';
  import { ToolTipFormatterParams } from '@/types/echarts';
  import useChartOption from '@/hooks/chart-option';
  import * as ECharts from 'echarts';

  const props = defineProps({
    loading: {
      type: Boolean,
      default: false,
    },
    data: {
      type: Object as () => any,
      required: true,
    },
    list: {
      type: Object as () => any,
      required: true,
    },
  });

  const emits = defineEmits(['fetchData']);

  const form = ref({
    reportType: 'daily',
    year: new Date().getFullYear(),
    time: new Date(),
    week: getWeekNumber(new Date()),
    month: new Date().getMonth() + 1,
    quarter: Math.floor(new Date().getMonth() / 3) + 1,
  });

  const handleReportTypeChange = () => {
    form.value.time = new Date();
    form.value.year = new Date().getFullYear();
    form.value.week = getWeekNumber(new Date());
    form.value.month = new Date().getMonth() + 1;
    form.value.quarter = Math.floor(new Date().getMonth() / 3) + 1;
  };
  const renderYear = ref(
    (() => {
      const years = [];
      // 近5年
      for (let i = 0; i < 5; i += 1) {
        const year = new Date().getFullYear() - i;
        years.push({
          value: year,
          text: year,
        });
      }
      return years;
    })()
  );
  const renderWeek = computed(() => {
    const weekNum = getNumOfWeeks(form.value.year);
    const firstSunday = new Date(form.value.year, 0, 1);
    const n = 6 - ((firstSunday.getDay() + 6) % 7);
    firstSunday.setDate(firstSunday.getDate() + n);
    const weeks = [];
    for (let i = 1; i <= weekNum; i += 1) {
      if (i === 1) {
        // 计算这年第一个周一的日期
        const firstMonday = new Date(
          firstSunday.setDate(firstSunday.getDate() - 6)
        );
        firstSunday.setDate(firstSunday.getDate() + 6);
        const value = `${getNowFormatDate(firstMonday)}-${getNowFormatDate(
          firstSunday
        )}`;
        weeks.push({
          value: i - 1,
          text: `第${i}周 （${value}）`,
        });
      } else {
        const value = getDateRange(firstSunday);
        weeks.push({
          value: i - 1,
          text: `第${i}周 （${value}）`,
        });
      }
      // firstSunday.setDate(firstSunday.getDate() + 7);
    }
    return weeks;
  });

  const fetchData = (data: ReportParams) => {
    emits('fetchData', data);
  };

  watch(
    () => form.value,
    () => {
      if (form.value.reportType === 'daily') {
        if (!isEmptyString(form.value.time)) {
          const startDate = moment(form.value.time)
            .startOf('day')
            .format('YYYY-MM-DD HH:mm:ss');
          const endDate = moment(form.value.time)
            .endOf('day')
            .format('YYYY-MM-DD HH:mm:ss');
          fetchData({
            reportType: form.value.reportType,
            startDate,
            endDate,
          });
        }
      }
      if (form.value.reportType === 'weekly') {
        const timeRange = (renderWeek.value[form.value.week].text as string)
          .split(' ')[1]
          .replace('（', '')
          .replace('）', '')
          .split('-');
        // 如果timeRange[0] 大于 timeRange[1]，则年份+1
        let plusYear = 0;
        if (
          moment(
            `${form.value.year}年${timeRange[0]}`,
            'YYYY年MM月DD日'
          ).isAfter(
            moment(`${form.value.year}年${timeRange[1]}`, 'YYYY年MM月DD日')
          )
        ) {
          plusYear = 1;
        }
        const startDate = moment(
          `${form.value.year}年${timeRange[0]}`,
          'YYYY年MM月DD日'
        ).format('YYYY-MM-DD HH:mm:ss');
        const endDate = moment(
          `${form.value.year + plusYear}年${timeRange[1]}`,
          'YYYY年MM月DD日'
        )
          .endOf('day')
          .format('YYYY-MM-DD HH:mm:ss');
        fetchData({
          reportType: form.value.reportType,
          startDate,
          endDate,
        });
      }
      if (form.value.reportType === 'monthly') {
        const startDate = moment(
          `${form.value.year}-${form.value.month}-01`,
          'YYYY-MM-DD'
        ).format('YYYY-MM-DD HH:mm:ss');
        const endDate = moment(
          `${form.value.year}-${form.value.month}`,
          'YYYY-MM'
        )
          .endOf('month')
          .format('YYYY-MM-DD HH:mm:ss');
        fetchData({
          reportType: form.value.reportType,
          startDate,
          endDate,
        });
      }
      if (form.value.reportType === 'quarterly') {
        // 季度报表，需要计算开始日期和结束日期
        // 季度第一天
        const startDate = moment(`${form.value.year}`, 'YYYY')
          .quarter(form.value.quarter)
          .startOf('quarter')
          .format('YYYY-MM-DD HH:mm:ss');
        // 季度最后一天
        const endDate = moment(`${form.value.year}`, 'YYYY')
          .quarter(form.value.quarter)
          .endOf('quarter')
          .format('YYYY-MM-DD HH:mm:ss');
        fetchData({
          reportType: form.value.reportType,
          startDate,
          endDate,
        });
      }
      if (form.value.reportType === 'yearly') {
        const startDate = moment(`${form.value.year}-01-01`).format(
          'YYYY-MM-DD HH:mm:ss'
        );
        const endDate = moment(`${form.value.year}`)
          .endOf('year')
          .format('YYYY-MM-DD HH:mm:ss');
        fetchData({
          reportType: form.value.reportType,
          startDate,
          endDate,
        });
      }
    },
    {
      deep: true,
      immediate: true,
    }
  );

  const tooltipItemsHtmlString = (items: ToolTipFormatterParams[]) => {
    return items
      .map(
        (el) => `<div class="content-panel">
        <p>
          <span style="background-color: ${
            el.color
          }" class="tooltip-item-icon"></span><span>${el.seriesName}</span>
        </p>
        <span class="tooltip-value">${el.value.toLocaleString()}</span>
      </div>`
      )
      .reverse()
      .join('');
  };

  const generateSeries = (
    name: string,
    lineColor: string,
    itemBorderColor: string,
    data: number[],
    yAxisIndex = 0
  ) => {
    return {
      name,
      data,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      yAxisIndex,
      itemStyle: {
        color: lineColor,
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          color: lineColor,
          borderWidth: 2,
          borderColor: itemBorderColor,
        },
      },
      lineStyle: {
        width: 2,
        color: lineColor,
      },
      showSymbol: false,
      areaStyle: {
        opacity: 0.1,
        color: lineColor,
      },
    } as ECharts.LineSeriesOption;
  };
  // const { t } = useI18n();

  const { isDark } = useThemes();
  const renderData = computed(() => [
    {
      title: '总耗电量',
      value: Number.parseFloat(props.data.energyConsumption ?? '0.0'),
      suffix: 'kWh',
      precision: 2,
      prefix: {
        icon: 'icon-fire',
        background: isDark.value ? '#593E2F' : '#FFE4BA',
        iconColor: isDark.value ? '#F29A43' : '#F77234',
      },
    },
    {
      title: '总航行距离',
      value: Number.parseFloat(props.data.sailDistance ?? '0.0'),
      precision: 2,
      suffix: 'km',
      prefix: {
        icon: 'icon-share-alt',
        background: isDark.value ? '#3D5A62' : '#E8FFFB',
        iconColor: isDark.value ? '#6ED1CE' : '#33D1C9',
      },
    },
    {
      title: '总航行时间',
      value: Number.parseFloat(props.data.sailDuration ?? '0.0') / 3600.0,
      precision: 2,
      suffix: 'h',

      prefix: {
        icon: 'icon-clock-circle',
        background: isDark.value ? '#354276' : '#E8F3FF',
        iconColor: isDark.value ? '#4A7FF7' : '#165DFF',
      },
    },
    {
      title: '二氧化碳排放量',
      value: Number.parseFloat(props.data.carbonEmission ?? '0.0'),
      precision: 2,
      suffix: 'ton',
      prefix: {
        icon: 'icon-share-internal',
        background: isDark.value ? '#3F385E' : '#F5E8FF',
        iconColor: isDark.value ? '#8558D3' : '#722ED1',
      },
    },
  ]);
  const xAxis = computed(() => {
    if (props.list.length === 0) return [];
    if (form.value.reportType === 'daily') {
      return props.list.map((item) => {
        return moment(item.time).format('YYYY-MM-DD');
      });
    }
    if (form.value.reportType === 'weekly') {
      return props.list.map((item) => {
        // return `${moment(item.time).format('YYYY')}年 第${getWeekNumber(
        //   moment(item.time).toDate()
        // )}周`;
        return moment(item.time).format('YYYY-MM-DD');
      });
    }
    if (form.value.reportType === 'monthly') {
      return props.list.map((item) => {
        return `${moment(item.time).format('YYYY')}年 ${moment(
          item.time
        ).format('M')}月`;
      });
    }
    if (form.value.reportType === 'quarterly') {
      return props.list.map((item) => {
        return `${moment(item.time).format('YYYY')}年 ${moment(
          item.time
        ).format('M')}月`;
      });
    }
    if (form.value.reportType === 'yearly') {
      return props.list.map((item) => {
        return `${moment(item.time).format('YYYY')}年`;
      });
    }
    return [];
  });
  const energyConsumptionData = computed(() => {
    if (props.list.length === 0) return [];
    return props.list.map((item) => {
      return Number.parseFloat(item.energyConsumption ?? '0.0');
    });
    // xAxis.value = props.list.map((item) => {
    //   return moment(item.date).format('YYYY-MM-DD');
    // });
  });
  const sailDistanceData = computed(() => {
    if (props.list.length === 0) return [];
    return props.list.map((item) => {
      return Number.parseFloat(item.sailDistance ?? '0.0');
    });
  });
  const sailDurationData = computed(() => {
    if (props.list.length === 0) return [];
    return props.list.map((item) => {
      return Number.parseFloat(item.sailDuration ?? '0.0') / 3600.0;
    });
  });
  const carbonEmissionData = computed(() => {
    if (props.list.length === 0) return [];
    return props.list.map((item) => {
      return Number.parseFloat(item.carbonEmission ?? '0.0');
    });
  });
  const { chartOption } = useChartOption((dark) => {
    return {
      grid: {
        left: '3.8%',
        right: '3.8%',
        top: '40',
        bottom: '40',
      },
      xAxis: {
        type: 'category',
        offset: 2,
        data: xAxis.value,
        boundaryGap: false,
        axisLine: {
          show: false,
        },
        axisLabel: {
          color: '#4E5969',
          formatter(value: number, idx: number) {
            // if (idx === 0) return '';
            // if (idx === xAxis.value.length - 1) return '';
            return `${value}`;
          },
        },
        axisTick: {
          show: false,
        },
        splitLine: {
          show: false,
        },
        axisPointer: {
          show: true,
          lineStyle: {
            color: '#23ADFF',
            width: 2,
          },
        },
      },
      yAxis: [
        {
          type: 'value',
          axisLine: {
            show: false,
          },
          show: false,
          axisLabel: {
            formatter(value: number, idx: number) {
              if (idx === 0) return String(value);
              return ``;
            },
          },
          splitLine: {
            lineStyle: {
              color: dark ? '#2E2E30' : '#F2F3F5',
            },
          },
        },
        {
          type: 'value',
          axisLine: {
            show: false,
          },
          show: false,
          splitLine: {
            lineStyle: {
              color: dark ? '#2E2E30' : '#F2F3F5',
            },
          },
        },
        {
          type: 'value',
          axisLine: {
            show: false,
          },
          show: false,
          splitLine: {
            lineStyle: {
              color: dark ? '#2E2E30' : '#F2F3F5',
            },
          },
        },
        {
          type: 'value',
          axisLine: {
            show: false,
          },
          show: false,
          splitLine: {
            lineStyle: {
              color: dark ? '#2E2E30' : '#F2F3F5',
            },
          },
        },
      ],
      tooltip: {
        trigger: 'axis',
        formatter(params) {
          const [firstElement] = params as ToolTipFormatterParams[];
          return `<div class="tooltip-container">
            <p class="tooltip-title">${firstElement.axisValueLabel}</p>
            ${tooltipItemsHtmlString(params as ToolTipFormatterParams[])}
          </div>`;
        },
        className: 'echarts-tooltip-diy',
      },
      // graphic: {
      //   elements: [
      //     {
      //       type: 'text',
      //       left: '2.6%',
      //       bottom: '18',
      //       style: {
      //         text: '12.10',
      //         textAlign: 'center',
      //         fill: '#4E5969',
      //         fontSize: 12,
      //       },
      //     },
      //     {
      //       type: 'text',
      //       right: '0',
      //       bottom: '18',
      //       style: {
      //         text: '12.17',
      //         textAlign: 'center',
      //         fill: '#4E5969',
      //         fontSize: 12,
      //       },
      //     },
      //   ],
      // },
      series: [
        generateSeries(
          '总耗电量',
          '#722ED1',
          '#F5E8FF',
          energyConsumptionData.value,
          0
        ),
        generateSeries(
          '总航行距离',
          '#F77234',
          '#FFE4BA',
          sailDistanceData.value,
          1
        ),
        generateSeries(
          '总航行时间',
          '#33D1C9',
          '#E8FFFB',
          sailDurationData.value,
          2
        ),
        generateSeries(
          'CO₂排放量',
          '#3469FF',
          '#E8F3FF',
          carbonEmissionData.value,
          3
        ),
      ],
    } as ECharts.EChartsOption;
  });
</script>

<style scoped lang="less">
  :deep(.arco-statistic) {
    .arco-statistic-title {
      color: rgb(var(--gray-10));
      font-weight: bold;
    }
    .arco-statistic-value {
      display: flex;
      align-items: center;
    }
  }
  :deep(.content-panel) {
    width: fit-content;
    min-width: 200px;
  }
  .statistic-prefix {
    display: inline-block;
    width: 32px;
    height: 32px;
    margin-right: 8px;
    color: var(--color-white);
    font-size: 16px;
    line-height: 32px;
    text-align: center;
    vertical-align: middle;
    border-radius: 6px;
  }
</style>
