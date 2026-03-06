<!-- CII预测图表 -->
<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: 0 }"
      :body-style="{
        paddingTop: '20px',
      }"
      :title="$t('workplace.navigationData')"
    >
      <template #extra>
        <a-link
          v-if="$route.name === 'Workplace'"
          @click="$router.push({ name: 'DataAnalysis' })"
          >{{ $t('workplace.viewMore') }}</a-link
        >
      </template>
      <Chart
        v-if="!isNavigationChartEmpty"
        height="289px"
        :option="navigationChartOption"
      />
      <a-empty v-else />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import useLoading from '@/hooks/loading';
  import {
    queryShipNavigationDataByDateTimeBetween,
    queryShipSocDataByDateTimeBetween,
  } from '@/api/dashboard';
  import useChartOption from '@/hooks/chart-option';
  import { computed } from 'vue';
  import { useShipDataStore } from '@/store';
  import { storeToRefs } from 'pinia';
  import moment from 'moment';
  import { workStatusText } from '@/types/global';
  import * as echarts from 'echarts';

  const shipDataStore = useShipDataStore();
  const { loading, setLoading } = useLoading(true);
  const { navData, socData } = storeToRefs(shipDataStore);
  const NavigationChartOptionFactory = () => {
    const seriesData = computed(() => {
      const batteryData = [];
      const speedData = [];
      const statusData = [];
      const logTimeData = [];
      navData.value.forEach((item, idx) => {
        logTimeData.push(moment(item.time).format('YYYY-MM-DD HH:mm:ss'));
        batteryData.push(socData.value?.at(idx)?.soc?.toFixed(2) || 0);
        speedData.push(item.speed);
        statusData.push(item.workStatus);
      });

      return {
        time: logTimeData,
        series: [
          {
            type: 'line',
            smooth: 0.6,
            symbol: 'rect',
            name: '电池剩余电量',
            lineStyle: {
              color: '#86ceff',
              width: 2,
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(80,141,255,0.39)',
                },
                {
                  offset: 0.34,
                  color: 'rgba(56,155,255,0.25)',
                },
                {
                  offset: 1,
                  color: 'rgba(38,197,254,0.00)',
                },
              ]),
            },
            data: batteryData,
          },
          {
            type: 'line',
            smooth: 0.4,
            symbol: 'circle',
            name: '航速',
            yAxisIndex: 1,
            lineStyle: {
              // 线性渐变，前四个参数分别是 x0, y0, x2, y2, 范围从 0 - 1，相当于在图形包围盒中的百分比，如果 globalCoord 为 `true`，则该四个值是绝对的像素位置
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 1,
                y2: 0,
                colorStops: [
                  {
                    offset: 0,
                    color: 'rgb(100, 235, 86)', // 0% 处的颜色
                  },
                  {
                    offset: 1,
                    color: '#28d016', // 100% 处的颜色
                  },
                ],
                globalCoord: false, // 缺省为 false
              },
              width: 2,
            },
            // areaStyle: {},
            data: speedData,
          },
          {
            type: 'custom',
            name: '工况',
            yAxisIndex: 1,
            data: statusData,
            renderItem: () => {
              /* empty */
            },
          },
        ],
      };
    });
    const isEmpty = computed(() => {
      return !(seriesData.value.time.length > 0);
    });
    const { chartOption } = useChartOption(() => {
      return {
        legend: {
          top: 'bottom',
          data: ['电池剩余电量', '航速'],
          textStyle: {
            color: '#4E5969',
          },
          selectedMode: false,
        },
        dataZoom: {
          type: 'inside', // 类型,滑动块插件
          show: true, // 是否显示下滑块
          xAxisIndex: [0], // 选择的x轴
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985',
            },
          },
          formatter: (params) => {
            const [batterySeries, speedSeries, statusSeries] = params as any;
            let time; // 声明变量
            if (batterySeries) {
              time = batterySeries.name; // 如果有电池数据，赋值为电池名称
            } else if (speedSeries) {
              time = speedSeries.name; // 如果没有电池数据但有速度数据，赋值为速度名称
            } else {
              time = '无数据'; // 如果都没有数据，赋值为 "无数据"
            }
            return `时间：${time}<br />电池剩余电量：${
              batterySeries ? batterySeries.value : '无数据'
            }%<br />航速：${
              speedSeries ? speedSeries.value : '无数据'
            } knot<br />工况：${
              statusSeries ? workStatusText[statusSeries.value] : '无数据'
            }`;
          },
        },
        grid: {
          left: '6%',
          right: '4%',
          top: '12%',
          bottom: '40',
        },
        xAxis: {
          type: 'category',
          name: '时间',
          boundaryGap: false,
          data: seriesData.value.time,
        },
        yAxis: [
          {
            type: 'value',
            name: '电池剩余电量 (%)',
            boundaryGap: [0, '30%'],
            max: 100,
            axisTick: {
              show: false,
            },
            splitLine: {
              show: false, // 不显示网格线
            },
          },
          {
            type: 'value',
            name: '航速 (knot)',
            position: 'right',
            boundaryGap: [0, '30%'],
            axisTick: {
              show: false,
            },
            splitLine: {
              show: false, // 不显示网格线
            },
          },
        ],

        // visualMap: {
        //   show: false,
        //   type: 'piecewise',
        //   dimension: 0,
        //   pieces: [
        //     {
        //       gt: 0,
        //       lt: 4,
        //       label: '停港',
        //       color: 'rgba(255, 125, 57, 0.5)',
        //     },
        //     {
        //       gt: 4,
        //       lt: 5,
        //       label: '航行中',
        //       color: 'rgba(53, 66, 185, 0.5)',
        //     },
        //     {
        //       gt: 5,
        //       lt: 8,
        //       label: '拖带',
        //       color: 'rgba(39, 173, 255, 0.5)',
        //     },
        //   ],
        // },
        series: seriesData.value.series as any,
      };
    });
    return {
      isEmpty,
      navData,
      chartOption,
    };
  };
  const {
    isEmpty: isNavigationChartEmpty,
    chartOption: navigationChartOption,
  } = NavigationChartOptionFactory();
  const fetchData = async () => {
    setLoading(true);
    try {
      const startDate = new Date();
      startDate.setHours(0, 0, 0, 0);
      const endDate = new Date();
      endDate.setHours(23, 59, 59, 999);
      const { data: navDataList } =
        await queryShipNavigationDataByDateTimeBetween({
          start: moment(startDate).format('YYYY-MM-DD HH:mm:ss'),
          end: moment(endDate).format('YYYY-MM-DD HH:mm:ss'),
        });
      const { data: socDataList } = await queryShipSocDataByDateTimeBetween({
        start: moment(startDate).format('YYYY-MM-DD HH:mm:ss'),
        end: moment(endDate).format('YYYY-MM-DD HH:mm:ss'),
      });
      shipDataStore.updateSocData(socDataList);
      shipDataStore.updateNavData(navDataList);
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };
  fetchData();
</script>

<style scoped lang="less"></style>
