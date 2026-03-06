<template>
  <a-card
    class="general-card"
    :title="$t('workplace.quick.operation')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '24px 20px 0 20px' }"
  >
    <a-row :gutter="8">
      <a-col
        v-for="link in links"
        :key="link.text"
        :span="8"
        class="wrapper"
        @click="handleLink(link.name)"
      >
        <div class="icon">
          <component :is="link.icon" />
        </div>
        <a-typography-paragraph class="text">
          {{ $t(link.text) }}
        </a-typography-paragraph>
      </a-col>
    </a-row>
    <a-divider class="split-line" style="margin: 0" />
  </a-card>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const links = [
    { text: 'workplace.noonReportEdit', icon: 'icon-file', name: 'NoonReport' },
    {
      text: 'workplace.contentStatistical',
      icon: 'icon-storage',
      name: 'DataAnalysis',
    },
    { text: 'workplace.shipManagement', icon: 'icon-settings', name: 'Info' },
  ];
  const handleLink = (name: string) => {
    router.push({ name });
  };
</script>

<style scoped lang="less"></style>
