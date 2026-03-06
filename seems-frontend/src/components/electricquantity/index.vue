<template>
  <div class="electric-quantity">
    <div class="electric-quantity-content">
      <span class="electric-quantity-title">剩余电量</span>
      <chart
        :options="electricChartOptions"
        :width="'450px'"
        :height="'200px'"
      ></chart>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import chart from '../chart/index.vue';

  const props = defineProps({
    electricQuantity: {
      type: Number,
      default: 0,
    },
  });

  const optionGenerator = (value: number) => {
    return {
      title: {
        show: true,
        left: 'left',
        textStyle: {
          fontSize: 18,
          fontWeight: '500',
          color: 'rgb(29, 33 , 41)',
        },
      },
      series: [
        {
          type: 'gauge',
          center: ['48%', '45%'],
          startAngle: 200,
          endAngle: -20,
          min: 0,
          max: 100,
          splitNumber: 10,
          itemStyle: {
            color: '#259544',
          },
          progress: {
            show: true,
            width: 10,
          },
          pointer: {
            show: false,
          },
          axisLine: {
            lineStyle: {
              width: 10,
            },
          },
          axisTick: {
            distance: 0,
            splitNumber: 5,
            lineStyle: {
              width: 2,
              color: '#999',
            },
            show: false,
          },
          splitLine: {
            show: false,
            distance: -10,
            length: 10,
            lineStyle: {
              width: 3,
              color: '#999',
            },
          },
          axisLabel: {
            show: false,
            distance: -20,
            color: '#999',
            fontSize: 10,
          },
          detail: {
            valueAnimation: true,
            width: '40%',
            lineHeight: 40,
            borderRadius: 8,
            offsetCenter: [0, '-10%'],
            fontSize: 20,
            fontWeight: 'bolder',
            formatter: '{value} %',
            color: 'inherit',
          },
          data: [
            {
              value: value.toFixed(2),
            },
          ],
        },
      ],
    };
  };

  const electricChartOptions = computed(() => {
    return optionGenerator(props.electricQuantity);
  });
</script>

<style scoped lang="less">
  .electric-quantity {
    position: relative;
    .electric-quantity-title {
      position: absolute;
      font-size: 18px;
      font-weight: 500;
      line-height: 1.5715;
      color: var(--color-text-1);
    }
  }
</style>
