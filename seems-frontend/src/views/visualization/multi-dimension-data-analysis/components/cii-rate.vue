<template>
  <a-card
    class="general-card"
    title="CII评级"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ display: 'flex', justifyContent: 'center' }"
  >
    <div v-if="loading" class="spin" style="height: 222px">
      <a-spin />
    </div>
    <div v-else class="cii-rate" :style="{ color: getCiiColor }">
      <div class="cii-rate-item">
        <div class="cii-rate-value">{{ CII }}</div>
      </div>
      <a-progress
        :percent="
          (137 - (props.data.cii * 100 > 137 ? 137 : props.data.cii * 100)) /
          100.0
        "
        :show-text="false"
        :stroke-width="15"
        :color="getCiiColor"
      />
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import useThemes from '@/hooks/themes';

  const { isDark } = useThemes();

  const props = defineProps({
    loading: {
      type: Boolean,
      default: false,
    },
    data: {
      type: Object,
      required: true,
    },
  });

  const getCiiColor = computed(() => {
    const { cii } = props.data;
    if (cii <= 0.66) {
      return isDark ? '#006F38' : '#7CFFB2';
    }
    if (cii <= 0.9) {
      return isDark ? '#006C7F' : '#58D9F9';
    }
    if (cii <= 1.11) {
      return isDark ? '#B99F1B' : '#FDDD60';
    }
    if (cii <= 1.37) {
      return isDark ? '#D24B5F' : '#FF6E76';
    }
    return isDark ? '#9B092B' : '#fe113a';
  });

  const CII = computed(() => {
    if (props.data && props.data.cii !== undefined) {
      if (props.data.cii < 0.66) {
        return 'A';
      }
      if (props.data.cii < 0.9) {
        return 'B';
      }
      if (props.data.cii > 1.11) {
        return 'C';
      }
      if (props.data.cii < 1.37) {
        return 'D';
      }
      return 'E';
    }
    return '';
  });
</script>

<style scoped lang="less">
  .spin {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .cii-rate {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 222px;
    width: 200px;
    .cii-rate-item {
      font-size: 64px;
      user-select: none;
    }
  }
</style>
