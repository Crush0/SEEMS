<template>
  <div class="flow-view">
    <div class="logo">
      <img src="/src/assets/images/logo/trans.png" width="120px" alt="logo" />
    </div>
    <a-card class="flow-card">
      <template #title>
        <p class="flow-title">首次创建账号，请加入船舶或新增船舶</p>
      </template>
      <a-tabs default-active-key="1" type="rounded" animation>
        <a-tab-pane key="1" :title="$t('menu.flow.join')">
          <JoinComponent />
        </a-tab-pane>
        <a-tab-pane
          key="2"
          :title="$t('menu.flow.create')"
          :disabled="roleStatus === '待审核' || roleStatus === '禁用'"
        >
          <CreateComponent />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { queryShipInfo } from '@/api/dashboard';
  import { computed, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useUserStore } from '@/store';
  import CreateComponent from './create/index.vue';
  import JoinComponent from './join/index.vue';

  const userStore = useUserStore();
  const roleStatus = computed(() => userStore.status);
  const router = useRouter();
  const checkShipRole = () => {
    return new Promise((resolve) => {
      queryShipInfo()
        .then(() => {
          resolve(true);
        })
        .catch(() => {
          resolve(false);
        });
    });
  };
  onMounted(() => {
    checkShipRole().then((hasRole) => {
      if (hasRole) {
        // 已加入船舶，跳转到船舶主页
        router.push({ name: 'Workplace' });
      }
    });
  });
</script>

<style scoped>
  .flow-view {
    display: flex;
    flex-direction: column;
    justify-content: center;
    height: 100vh;
    align-items: center;
  }
  .flow-card {
    border-radius: 4px;
    width: 1000px;
    margin: 0 auto;
  }
  .flow-title {
    font-size: 20px;
    font-weight: bold;
    text-align: center;
  }
</style>
