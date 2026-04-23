<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      title="电能消耗趋势分析"
    >
      <Chart style="width: 100%; height: 300px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { queryDataAnalysis } from '@/api/visualization';
  import useLoading from '@/hooks/loading';
  import useChartOption from '@/hooks/chart-option';

  const { loading, setLoading } = useLoading(true);

  const { chartOption } = useChartOption((isDark) => {
    return {
      legend: {
        left: 'center',
        data: ['日耗电量', '单位运输功能耗', '单位距离能耗'],
        bottom: 0,
        icon: 'circle',
        itemWidth: 8,
        textStyle: {
          color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969',
        },
        itemStyle: {
          borderWidth: 0,
        },
      },
      tooltip: {
        show: true,
        trigger: 'axis',
      },
      grid: {
        left: '5%',
        right: '5%',
        top: '15%',
        bottom: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
        boundaryGap: false,
        axisLine: {
          lineStyle: {
            color: isDark ? '#484849' : '#E5E8EF',
          },
        },
        axisLabel: {
          color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969',
        },
      },
      yAxis: {
        type: 'value',
        axisLine: {
          show: false,
        },
        axisLabel: {
          color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969',
        },
        splitLine: {
          lineStyle: {
            color: isDark ? '#484849' : '#E5E8EF',
          },
        },
      },
      series: [
        {
          name: '日耗电量',
          data: [120, 132, 101, 134, 90, 230],
          type: 'line',
          smooth: true,
          showSymbol: false,
          lineStyle: {
            width: 2,
            color: '#165DFF',
          },
          itemStyle: {
            color: '#165DFF',
          },
        },
        {
          name: '单位运输功能耗',
          data: [0.15, 0.23, 0.18, 0.29, 0.12, 0.35],
          type: 'line',
          smooth: true,
          showSymbol: false,
          lineStyle: {
            width: 2,
            color: '#00B42A',
          },
          itemStyle: {
            color: '#00B42A',
          },
        },
        {
          name: '单位距离能耗',
          data: [2.5, 3.2, 2.8, 3.5, 2.1, 4.2],
          type: 'line',
          smooth: true,
          showSymbol: false,
          lineStyle: {
            width: 2,
            color: '#FF7D00',
          },
          itemStyle: {
            color: '#FF7D00',
          },
        },
      ],
    };
  });

  const fetchTrendData = async () => {
    setLoading(true);
    try {
      const { data } = await queryDataAnalysis();
      // 这里可以用真实数据更新图表
      console.log('电能消耗趋势数据:', data);
    } catch (error) {
      console.error('获取电能消耗趋势数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  onMounted(() => {
    fetchTrendData();
  });
</script>

<style scoped lang="less">
  :deep(.arco-card-body) {
    padding: 16px;
  }
</style>
