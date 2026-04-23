<template>
  <a-card
    class="data-statistic"
    :bordered="false"
    :body-style="{ padding: '20px' }"
  >
    <template #title>
      <div class="data-statistic-header">{{ shipInfo.name }}</div>
    </template>
    <div class="data-statistic-content">
      <a-row class="grid" :gutter="6" justify="center" :wrap="false">
        <a-col class="grid-item" :flex="4">
          <a-card
            class="data-statistic-list"
            style="width: auto"
            :body-style="{
              padding: '0',
            }"
          >
            <template #title>
              <p class="title">左推进器</p>
            </template>
            <Propeller :data="realTimeData.leftPropeller" />
          </a-card>
        </a-col>
        <a-col class="grid-item" :flex="'auto'">
          <a-card
            class="data-statistic-list"
            style="padding: 8px"
            :body-style="{
              padding: '0',
            }"
          >
            <template #title>
              <p class="title">船舶信息</p>
            </template>
            <a-space direction="vertical" fill :align="'center'">
              <div class="compass-container">
                <Suspense>
                  <Compass class="compass" />
                </Suspense>
              </div>
              <div class="gps">
                <div class="header">
                  <AIconFont
                    type="icon-yanjizhushou-shangchuan_GPS"
                    :size="16"
                  />
                  <span class="title">GPS</span>
                </div>
                <div class="content">
                  {{
                    formatCoordinates(
                      realTimeData.position?.latitude ?? 0,
                      realTimeData.position?.longitude ?? 0
                    )
                  }}
                </div>
                <div class="footer">
                  {{ realTimeData.position?.latitude ?? 0 }},{{
                    realTimeData.position?.longitude ?? 0
                  }}
                </div>
              </div>

              <div class="detail-info">
                <a-space direction="vertical" fill>
                  <a-row class="info-item">
                    <a-col :span="8">
                      <a-statistic
                        title="对地航向"
                        :value="realTimeData.direction ?? undefined"
                        placeholder="NaN°"
                        :precision="1"
                      >
                        <template #suffix><sup>°</sup></template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="航速"
                        :value="realTimeData.speed ?? 0"
                        :precision="1"
                      >
                        <template #suffix><sub>knot</sub></template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="预计续航"
                        :value="
                          ((realTimeData.leftBatteryCapacity ?? 0) +
                            (realTimeData.rightBatteryCapacity ?? 0)) *
                          0.755
                        "
                        :precision="1"
                      >
                        <template #suffix><sub>km</sub></template>
                      </a-statistic>
                    </a-col>
                  </a-row>
                  <a-row class="info-item">
                    <a-col :span="8">
                      <a-statistic
                        title="今日航程"
                        :precision="2"
                        :value="realTimeData.sailRange ?? 0"
                      >
                        <template #suffix><sub>km</sub></template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="航行时间"
                        :value="(realTimeData.sailDuration ?? 0) / 3600.0"
                        :precision="2"
                      >
                        <template #suffix><sub>h</sub></template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="功耗"
                        :precision="2"
                        :value="realTimeData.powerDissipation ?? 0"
                      >
                        <template #suffix><sub>kwh/km</sub></template>
                      </a-statistic>
                    </a-col>
                  </a-row>
                  <a-row class="info-item">
                    <a-col :span="8">
                      <a-statistic
                        title="风速"
                        :value="realTimeData.windSpeed ?? 0"
                        style="margin-top: 10px"
                        :precision="1"
                      >
                        <template #suffix><sub>m/s</sub></template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="8">
                      <div class="work-status">
                        <div class="work-status-title"> 当前工况 </div>
                        <div class="work-status-content">
                          <div class="work-status-value">
                            <a-tag
                              :color="
                                workStatusColor[
                                  realTimeData.workStatus ?? WorkStatus.UNKNOWN
                                ]
                              "
                              size="large"
                            >
                              {{
                                workStatusText[
                                  realTimeData.workStatus ?? WorkStatus.UNKNOWN
                                ]
                              }}
                            </a-tag>
                          </div>
                        </div>
                      </div>
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="风向"
                        :value="realTimeData.windDirection ?? 0"
                        :precision="1"
                        style="margin-top: 10px"
                      >
                        <template #suffix><sup>°</sup></template>
                      </a-statistic>
                    </a-col>
                  </a-row>
                </a-space>
              </div>
            </a-space>

            <a-divider :margin="5" />
            <a-row class="battery" :gutter="6">
              <a-col :span="12">
                <a-card class="data-statistic-list">
                  <template #title>
                    <div class="header"> 左电仓电量 </div>
                  </template>
                  <div class="battery-box" style="height: 45px">
                    <BatteryIcon
                      :quantity="realTimeData.leftBatteryCapacity ?? 0"
                    />
                  </div>
                </a-card>
              </a-col>
              <a-col :span="12"
                ><a-card class="data-statistic-list">
                  <template #title>
                    <div class="header"> 右电仓电量 </div>
                  </template>
                  <div class="battery-box" style="height: 45px">
                    <BatteryIcon
                      :quantity="realTimeData.rightBatteryCapacity ?? 0"
                    />
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
        <a-col class="grid-item" :flex="4">
          <a-card
            class="data-statistic-list"
            :body-style="{
              padding: '0',
            }"
            style="width: auto"
          >
            <template #title>
              <p class="title">右推进器</p>
            </template>
            <Propeller :data="realTimeData.rightPropeller" />
          </a-card>
        </a-col>
      </a-row>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  import AIconFont from '@/components/AIconFont.vue';
  import BatteryIcon from '@/components/BatteryIcon.vue';
  import { RealTimeData } from '@/store/modules/ship-data/type';
  import { WorkStatus, workStatusText } from '@/types/global';
  import Propeller from './propeller.vue';
  import Compass from './compass.vue';

  const { realTimeData } = defineProps({
    realTimeData: {
      type: Object as () => RealTimeData,
    },
    shipInfo: {
      type: Object as () => any,
    },
  });
  function formatCoordinates(lat, lon) {
    const formatDMS = (degrees) => {
      const d = Math.floor(degrees);
      const min = Math.floor((degrees - d) * 60);
      const sec = ((degrees - d - min / 60) * 3600).toFixed(4);
      return `${d}°${min}′${sec}″`;
    };

    const formattedLat = formatDMS(Math.abs(lat)) + (lat >= 0 ? 'N' : 'S');
    const formattedLon = formatDMS(Math.abs(lon)) + (lon >= 0 ? 'E' : 'W');

    return `${formattedLat} ${formattedLon}`;
  }

  const workStatusColor = {
    [WorkStatus.DRAGGING]: 'blue',
    [WorkStatus.HOVERING]: 'green',
    [WorkStatus.STOPPING_AT_PORT]: 'orange',
    [WorkStatus.UNKNOWN]: 'gray',
    [WorkStatus.CHARGING]: '#136a9c',
    [WorkStatus.IDLE]: '#999',
  };
</script>

<style scoped lang="less">
  .data-statistic {
    background-color: transparent;
    &:deep(.arco-card-body) {
      background-color: transparent;
    }
  }
  .work-status {
    display: inline-block;
    color: var(--color-text-2);
    line-height: 1.5715;
    .work-status-title {
      margin-bottom: 8px;
      color: var(--color-text-2);
      font-size: 14px;
    }
    .work-status-content {
      .work-status-value {
        color: var(--color-text-1);
        font-weight: 500;
        font-size: 26px;
        white-space: nowrap;
        text-align: center;
      }
    }
  }
  .grid {
    .data-statistic-list {
      // border: 1px solid #e8e8e8;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      border-radius: 4px;
      padding: 16px;
      text-align: center;
      font-size: 14px;
      font-weight: bold;
      // color: #333;
    }
  }
  .compass-container {
    width: 150px;
    height: 150px;
  }
  .data-statistic-header {
    width: 100%;
    text-align: center;
    font-weight: bolder;
  }
  .data-statistic-content {
    background-color: transparent;
  }
  .data-statistic {
    &-content {
      padding: 20px 0;
      .header {
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .title {
        font-size: 14px;
        font-weight: bolder;
      }
      .content {
        font-size: 16px;
        font-weight: bold;
        // color: #333;
        text-align: center;
      }
      .footer {
        text-align: center;
      }
    }

    &-list {
      &-header {
        display: flex;
        justify-content: space-between;
        margin-top: 16px;
      }

      &-content {
        margin-top: 16px;
      }
    }
  }
  .info-item {
    .arco-col {
      height: 65px;
      width: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      margin-top: 10px;
      margin-bottom: 10px;
    }
    .arco-col:not(:last-child)::after {
      position: absolute;
      content: '';
      display: block;
      height: 100%;
      border: 0.5px dashed #e8e8e8;
      margin-top: 10px;
      margin-bottom: 10px;
      right: 0;
    }
    &:deep(.arco-statistic-value) {
      text-align: center;
    }
  }
</style>
