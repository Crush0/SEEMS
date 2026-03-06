<template>
  <div class="propeller-box">
    <AIconFont
      :type="isDark ? 'icon-luoxuanjiang1' : 'icon-luoxuanjiang2'"
      size="128"
      style="margin: 15px"
      :spin="
        (data
          ? data.status ?? PropellerWorkStatus.UNKNOWN
          : PropellerWorkStatus.UNKNOWN) === PropellerWorkStatus.RUNNING
      "
    />
    <a-typography-title :heading="6"> 推进器状态 </a-typography-title>
    <span
      class="propeller-status"
      :style="{
        color: StatusColorMap[data ? data.status : PropellerWorkStatus.UNKNOWN],
      }"
      >{{ StatusMap[data ? data.status : PropellerWorkStatus.UNKNOWN] }}</span
    >
    <a-divider />
    <a-row class="info-item">
      <a-col :span="8">
        <a-statistic title="功率" :value="data ? data.power : 0" :precision="1">
          <template #extra>kw</template>
        </a-statistic>
      </a-col>
      <a-col :span="8">
        <a-statistic
          title="舵角"
          :value="data ? data.degrees : 0"
          :precision="1"
        >
          <template #extra>deg</template>
        </a-statistic>
      </a-col>
      <a-col :span="8">
        <a-statistic title="转速" :value="data ? data.rpm : 0" :precision="1">
          <template #extra>rpm</template>
        </a-statistic>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import AIconFont from '@/components/AIconFont.vue';
  import useThemes from '@/hooks/themes';
  import { PropellerWorkStatus } from '@/store/modules/ship-data/type';

  defineProps({
    data: {
      type: Object,
    },
  });
  const { isDark } = useThemes();
  //   export enum PropellerWorkStatus {
  //   RUNNING = 'running',
  //   STOPPED = 'stopped',
  //   ERROR = 'error',
  //   UNKNOWN = 'unknown',
  // }
  const StatusMap = {
    [PropellerWorkStatus.RUNNING]: '运行中',
    [PropellerWorkStatus.STOPPED]: '停止',
    [PropellerWorkStatus.ERROR]: '故障',
    [PropellerWorkStatus.UNKNOWN]: '未知',
  };
  const StatusColorMap = {
    [PropellerWorkStatus.RUNNING]: '#52c41a',
    [PropellerWorkStatus.STOPPED]: '#909399',
    [PropellerWorkStatus.ERROR]: '#f5222d',
    [PropellerWorkStatus.UNKNOWN]: '#909399',
  };
</script>

<style lang="less" scoped>
  .propeller-status {
    font-size: 24px;
  }
</style>
