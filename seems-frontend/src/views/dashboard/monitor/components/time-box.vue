<template>
  <a-card title="时间">
    <div class="time-box">
      <a-statistic :value="new Date(time)" format="HH:mm:ss" />
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  defineProps({
    time: {
      type: Number,
      required: true,
    },
  });
</script>

<style scoped>
  .time-box {
    display: flex;
    justify-content: center;
    align-items: center;
  }
</style>
