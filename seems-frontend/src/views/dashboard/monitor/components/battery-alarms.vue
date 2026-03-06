<template>
  <a-card
    :body-style="{
      padding: '0',
    }"
  >
    <a-typography-title :heading="6" style="text-align: center">
      {{ title }}
    </a-typography-title>
    <a-scrollbar
      style="height: 230px; overflow: auto; text-align: center; width: 100%"
    >
      <a-avatar
        v-for="(alarm, index) in alarms"
        :key="index"
        :size="35"
        style="margin: 10px"
        :style="{
          backgroundColor: alarm ? '#f5222d' : '#c9cdd4',
          cursor: 'default',
          userSelect: 'none',
        }"
      >
        {{ index + 1 }}
      </a-avatar>
    </a-scrollbar>
  </a-card>
</template>

<script setup>
  defineProps({
    alarms: {
      type: Array,
      default: () => [],
    },
    title: {
      type: String,
      default: '',
    },
  });
</script>

<style scoped>
  .battery-alarms {
    padding: 10px 0;
  }
</style>
