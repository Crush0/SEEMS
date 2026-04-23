<template>
  <div>
    <a-row :gutter="16">
      <a-col :span="6">
        <ChainItem
          :loading="loading"
          :data="unitDistanceEnergyConsumption"
          title="单位航行距离电能消耗量"
          chart-type="line"
        />
      </a-col>
      <a-col :span="6">
        <ChainItem
          :loading="loading"
          :data="preUnitWorkEnergyConsumption"
          title="单位运输功电能消耗量"
          chart-type="line"
        />
      </a-col>
      <a-col :span="6">
        <ChainItem
          :loading="loading"
          :data="preHourEnergyConsumption"
          title="单位小时耗电量"
          chart-type="line"
        />
      </a-col>
      <a-col :span="6">
        <ChainItem
          :loading="loading"
          :data="unitDistanceCO2Emission"
          title="单位航行距离二氧化碳排放量"
          chart-type="line"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, ref, watch } from 'vue';
  import ChainItem from './chain-item.vue';

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
  const unitDistanceEnergyConsumption = ref({
    data: 0,
    list: [],
  });
  const preUnitWorkEnergyConsumption = ref({
    data: 0,
    list: [],
  });
  const preHourEnergyConsumption = ref({
    data: 0,
    list: [],
  });
  const unitDistanceCO2Emission = ref({
    data: 0,
    list: [],
  });
  watch(
    () => props,
    () => {
      if (props.data && props.list && props.data.analyzeData) {
        unitDistanceEnergyConsumption.value = {
          data: props.data.analyzeData.preDistanceEnergyConsumption,
          list: props.list.map(
            (item) => item.analyzeData?.preDistanceEnergyConsumption ?? null
          ),
        };
        preUnitWorkEnergyConsumption.value = {
          data: props.data.analyzeData.preUnitWorkEnergyConsumption,
          list: props.list.map(
            (item) => item.analyzeData?.preUnitWorkEnergyConsumption ?? null
          ),
        };
        preHourEnergyConsumption.value = {
          data: props.data.analyzeData.preHourEnergyConsumption,
          list: props.list.map(
            (item) => item.analyzeData?.preHourEnergyConsumption ?? null
          ),
        };
        unitDistanceCO2Emission.value = {
          data: props.data.analyzeData.preDistanceEnergyConsumption,
          list: props.list.map(
            (item) => item.analyzeData?.preDistanceEnergyConsumption ?? null
          ),
        };
      }

      // // 单位运输功电能消耗量
      // const preUnitWorkEnergyConsumption = computed(() => {
      //   return {
      //     data: props.data.analyzeData.preUnitWorkEnergyConsumption,
      //     list: props.list.map(
      //       (item) => item.analyzeData.preUnitWorkEnergyConsumption
      //     ),
      //   };
      // });
      // // 单位小时耗电量
      // const preHourEnergyConsumption = computed(() => {
      //   return {
      //     data: props.data.analyzeData.preHourEnergyConsumption,
      //     list: props.list.map((item) => item.analyzeData.preHourEnergyConsumption),
      //   };
      // });
      // // 单位航行距离二氧化碳排放量
      // const unitDistanceCO2Emission = computed(() => {
      //   return {
      //     data: props.data.analyzeData.preDistanceEnergyConsumption,
      //     list: props.list.map((item) => item.analyzeData.preDistanceEnergyConsumption),
      //   };
      // });
    },
    {
      deep: true,
    }
  );

  onMounted(() => {
    console.log(props.data);
  });
</script>
