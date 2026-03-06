<template>
  <a-descriptions
    :data="data"
    :column="2"
    size="large"
    style="margin-left: 20px"
  >
  </a-descriptions>
</template>

<script lang="ts" setup>
  import { useShipDataStore } from '@/store';
  import { storeToRefs } from 'pinia';
  import { computed } from 'vue';

  const shipDataStore = useShipDataStore();
  const { shipInfo } = storeToRefs(shipDataStore);
  const data = computed(() => [
    {
      label: '船舶名称',
      value: shipInfo.value.name === '' ? '暂无数据' : shipInfo.value.name,
    },
    {
      label: '船舶类型',
      value: shipInfo.value.type === '' ? '暂无数据' : shipInfo.value.type,
    },
    {
      label: '船舶型号',
      value: shipInfo.value.model === '' ? '暂无数据' : shipInfo.value.model,
    },
    {
      label: '船舶隶属公司',
      value:
        shipInfo.value.ownerCompany === ''
          ? '暂无数据'
          : shipInfo.value.ownerCompany,
    },
    {
      label: '船舶制造商',
      value:
        shipInfo.value.manufacturer === ''
          ? '暂无数据'
          : shipInfo.value.manufacturer,
    },
    {
      label: '船舶初始登记号',
      value:
        shipInfo.value.shipOriginNumber === ''
          ? '暂无数据'
          : shipInfo.value.shipOriginNumber,
    },
    {
      label: 'MMSI号',
      value: shipInfo.value.mmsi === '' ? '暂无数据' : shipInfo.value.mmsi,
    },
    {
      label: 'IMO号',
      value:
        shipInfo.value.imoNumber === '' ? '暂无数据' : shipInfo.value.imoNumber,
    },
    {
      label: '船舶总长',
      value:
        shipInfo.value.length === 0 ? '暂无数据' : `${shipInfo.value.length}m`,
    },
    {
      label: '垂线间长',
      value:
        shipInfo.value.lbpLength === 0
          ? '暂无数据'
          : `${shipInfo.value.lbpLength}m`,
    },
    {
      label: '设计水线长',
      value:
        shipInfo.value.designLwl === 0
          ? '暂无数据'
          : `${shipInfo.value.designLwl}m`,
    },
    {
      label: '型宽',
      value:
        shipInfo.value.moldedWidth === 0
          ? '暂无数据'
          : `${shipInfo.value.moldedWidth}m`,
    },
    {
      label: '船舶宽度',
      value:
        shipInfo.value.width === 0 ? '暂无数据' : `${shipInfo.value.width}m`,
    },
    {
      label: '型深',
      value:
        shipInfo.value.moldedDepth === 0
          ? '暂无数据'
          : `${shipInfo.value.moldedDepth}m`,
    },
    {
      label: '设计吃水',
      value:
        shipInfo.value.designDraft === 0
          ? '暂无数据'
          : `${shipInfo.value.designDraft}m`,
    },
    {
      label: '最大吃水',
      value:
        shipInfo.value.maxDraft === 0
          ? '暂无数据'
          : `${shipInfo.value.maxDraft}m`,
    },
    {
      label: '空载押水量',
      value:
        shipInfo.value.noLoadDraft === 0
          ? '暂无数据'
          : `${shipInfo.value.noLoadDraft}吨`,
    },
    {
      label: '满载押水量',
      value:
        shipInfo.value.maxLoadDraft === 0
          ? '暂无数据'
          : `${shipInfo.value.maxLoadDraft}吨`,
    },
    {
      label: '船舶吨位',
      value:
        shipInfo.value.progress === 0
          ? '暂无数据'
          : `${shipInfo.value.progress}吨`,
    },
    {
      label: '船舶拖力',
      value:
        shipInfo.value.towingForce === 0
          ? '暂无数据'
          : `${shipInfo.value.towingForce}ton`,
    },
    {
      label: '船舶最大电池容量',
      value:
        shipInfo.value.maxBatteryCapacity === 0
          ? '暂无数据'
          : `${shipInfo.value.maxBatteryCapacity}kwh`,
    },
  ]);
</script>

<style scoped></style>
