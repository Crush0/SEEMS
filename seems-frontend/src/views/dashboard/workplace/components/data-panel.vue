<template>
  <a-grid :cols="24" :row-gap="16" class="panel">
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
    >
      <a-card
        :bordered="false"
        :style="{
          height: '150px',
          width: '400px',
          borderRadius: '4px',
          background: isDark
            ? 'linear-gradient(180deg, #284991 0%, #122B62 100%)'
            : 'linear-gradient(180deg, #f2f9fe 0%, #e6f4fe 100%)',
        }"
      >
        <a-descriptions :data="shipDataDesc" layout="inline-horizontal">
          <template #title>
            <div
              style="
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: space-between;
              "
            >
              <span class="card-title">
                {{ shipData.name }}

                <a-tooltip :content="`船舶ID：${shipData.id}`">
                  <a-button
                    shape="circle"
                    type="text"
                    @click="copyShipIdToClipboard"
                  >
                    <template #icon>
                      <icon-info />
                    </template>
                  </a-button>
                </a-tooltip>
              </span>
            </div>
          </template>
        </a-descriptions>
      </a-card>
    </a-grid-item>
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
    >
      <a-card
        :bordered="false"
        :style="{
          height: '150px',
          width: '400px',
          borderRadius: '4px',
          background: isDark
            ? ' linear-gradient(180deg, #3D492E 0%, #263827 100%)'
            : 'linear-gradient(180deg, #F5FEF2 0%, #E6FEEE 100%)',
        }"
      >
        <electricquantity
          :electric-quantity="
            ((realTimeData.leftBatteryCapacity ?? 0) +
              (realTimeData.rightBatteryCapacity ?? 0)) /
            2
          "
        />
      </a-card>
    </a-grid-item>
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
    >
      <a-card
        :bordered="false"
        :style="{
          height: '150px',
          width: '400px',
          borderRadius: '4px',
          background: isDark
            ? 'linear-gradient(180deg, #312565 0%, #201936 100%)'
            : 'linear-gradient(180deg, #F7F7FF 0%, #ECECFF 100%)',
        }"
      >
        <template #title>
          <div
            style="
              width: 100%;
              height: 100%;
              display: flex;
              align-items: center;
              justify-content: space-between;
            "
          >
            <span class="card-title"> 预估续航 </span>
          </div>
        </template>
        <a-statistic
          :value="
            ((realTimeData.leftBatteryCapacity ?? 0) +
              (realTimeData.rightBatteryCapacity ?? 0)) *
            0.755
          "
          :precision="1"
        >
          <template #suffix>km</template>
        </a-statistic>
      </a-card>
    </a-grid-item>
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
    >
      <a-card
        :bordered="false"
        :style="{
          height: '150px',
          width: '400px',
          borderRadius: '4px',
          background: isDark
            ? 'linear-gradient(180deg, #294B94 0%, #0F275C 100%)'
            : 'linear-gradient(180deg, #f2f9fe 0%, #e6f4fe 100%)',
        }"
      >
        <template #title>
          <div
            style="
              width: 100%;
              height: 100%;
              display: flex;
              align-items: center;
              justify-content: space-between;
            "
          >
            <span class="card-title"> 今日耗电量 </span>
          </div>
        </template>
        <!-- <div class="card-body" :style="{ color: CIILvlOptions[CIILvl].color }">
          {{ CIILvlOptions[CIILvl].text }}
        </div> -->
        <a-statistic
          :value="dailyConsumption"
          show-group-separator
          animation
          :precision="2"
        >
          <template #suffix>kwh</template>
        </a-statistic>
      </a-card>
    </a-grid-item>
    <a-grid-item :span="24">
      <a-divider class="panel-border" />
    </a-grid-item>
  </a-grid>
</template>

<script lang="ts" setup>
  import { DescData, Message } from '@arco-design/web-vue';
  import { computed, ref } from 'vue';
  import useThemes from '@/hooks/themes';
  import electricquantity from '@/components/electricquantity/index.vue';
  import { useShipDataStore } from '@/store';
  import { queryShipInfo } from '@/api/dashboard';
  import { storeToRefs } from 'pinia';
  import { queryDataAnalysis } from '@/api/visualization';
  import { copyToClipboard } from '@/utils/clipboard';

  const shipDataStore = useShipDataStore();

  const { isDark } = useThemes();
  const { realTimeData, shipInfo: shipData } = storeToRefs(shipDataStore);
  const dailyConsumption = ref(0);
  const copyShipIdToClipboard = () => {
    copyToClipboard(shipData.value.id)
      .then(() => {
        Message.success('船舶ID已复制到剪贴板');
      })
      .catch(() => {
        Message.error('复制失败, 请手动复制');
      });
  };
  const shipDataDesc = computed(() =>
    (
      [
        {
          label: '初始登记号',
          value: shipData.value.shipOriginNumber,
        },
        {
          label: '船型',
          value: shipData.value.type,
        },
        {
          label: '吨位',
          value: `${shipData.value.progress}DWT`,
        },
        shipData.value.type === '拖轮'
          ? {
              label: '拖力',
              value: `${shipData.value.towingForce}/t`,
            }
          : undefined,
        {
          label: '最大电池容量',
          value: `${shipData.value.maxBatteryCapacity}kwh`,
        },
      ] as DescData[]
    ).filter(Boolean)
  );
  const fetchData = async () => {
    const { data } = await queryShipInfo();
    shipDataStore.updateShipInfo(data);
    const { data: analysisDataList } = await queryDataAnalysis();
    if (
      typeof analysisDataList[analysisDataList.length - 1]
        .dailyEnergyConsumption === 'string'
    ) {
      dailyConsumption.value = Number.parseFloat(
        analysisDataList[analysisDataList.length - 1]
          .dailyEnergyConsumption as string
      );
    } else {
      dailyConsumption.value = analysisDataList[analysisDataList.length - 1]
        .dailyEnergyConsumption as number;
    }
  };

  fetchData();
</script>

<style lang="less" scoped>
  .card-title {
    font-size: 18px;
    font-weight: 500;
  }
  .arco-grid.panel {
    margin-bottom: 0;
    padding: 16px 20px 0 20px;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }
  .card-body {
    font-size: 30px;
    font-weight: 500;
    margin-top: 12px;
    text-align: center;
  }
  .panel-col {
    padding-left: 43px;
    border-right: 1px solid rgb(var(--gray-2));
    border-radius: 12px;
  }
  .col-avatar {
    margin-right: 12px;
    background-color: var(--color-fill-2);
  }
  .up-icon {
    color: rgb(var(--red-6));
  }
  .unit {
    margin-left: 8px;
    color: rgb(var(--gray-8));
    font-size: 12px;
  }
  :deep(.panel-border) {
    margin: 4px 0 0 0;
  }
</style>
