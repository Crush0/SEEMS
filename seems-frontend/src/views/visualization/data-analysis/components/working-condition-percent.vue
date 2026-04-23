<!-- 内容类型占比 -->
<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{
        padding: '20px',
      }"
    >
      <template #title>
        {{ $t('workplace.workingConditionPercent') }}
      </template>
      <Chart height="289px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import useLoading from '@/hooks/loading';
  import useChartOption from '@/hooks/chart-option';
  import { durationTime2Str } from '@/utils/time-format';
  import { queryWorkConditionDurationByDateTimeBetween } from '@/api/dashboard';
  import moment from 'moment';
  import { ref } from 'vue';

  const workConditionData = ref({} as any);
  const { loading } = useLoading();

  const { chartOption } = useChartOption((isDark) => {
    // echarts support https://echarts.apache.org/zh/theme-builder.html
    // It's not used here
    return {
      legend: {
        left: 'center',
        data: ['拖带', '航行', '停港', '充电', '未知', '待命'],
        icon: 'circle',
        itemWidth: 8,
        textStyle: {
          color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#4E5969',
        },
        itemStyle: {
          borderWidth: 0,
        },
      },
      tooltip: {
        show: true,
        trigger: 'item',
        formatter: (params) => {
          return `${params.name}时间： ${durationTime2Str(params.value[0])}`;
        },
      },
      series: [
        {
          type: 'pie',
          radius: ['50%', '70%'],
          center: ['50%', '50%'],
          label: {
            formatter: '{b} {d}%',
            fontSize: 14,
            color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#4E5969',
          },
          itemStyle: {
            borderColor: isDark ? '#232324' : '#fff',
            borderWidth: 1,
          },
          data: [
            {
              value: [workConditionData.value.DRAGGING],
              name: '拖带',
              itemStyle: {
                color: isDark ? '#3D72F6' : '#249EFF',
              },
            },
            {
              value: [workConditionData.value.HOVERING],
              name: '航行',
              itemStyle: {
                color: isDark ? '#A079DC' : '#313CA9',
              },
            },
            {
              value: [workConditionData.value.STOPPING_AT_PORT],
              name: '停港',
              itemStyle: {
                color: isDark ? '#f7b4d7' : '#f77234',
              },
            },
            {
              value: [workConditionData.value.IDLE],
              name: '待命',
              itemStyle: {
                color: isDark ? '#fff' : '#999',
              },
            },
            {
              value: [workConditionData.value.UNKNOWN],
              name: '未知',
              itemStyle: {
                color: isDark ? '#fff' : '#999',
              },
            },
            {
              value: [workConditionData.value.CHARGING],
              name: '充电',
              itemStyle: {
                color: isDark ? '#4ec9b0' : '#10bc5a',
              },
            },
          ],
        },
      ],
    };
  });
  const fetchData = async () => {
    const startDate = new Date();
    startDate.setHours(0, 0, 0, 0);
    const endDate = new Date();
    endDate.setHours(23, 59, 59, 999);
    queryWorkConditionDurationByDateTimeBetween({
      start: moment(startDate).format('YYYY-MM-DD HH:mm:ss'),
      end: moment(endDate).format('YYYY-MM-DD HH:mm:ss'),
    }).then((res) => {
      workConditionData.value = res.data.workConditionDuration;
    });
  };
  fetchData();
</script>

<style scoped lang="less"></style>
