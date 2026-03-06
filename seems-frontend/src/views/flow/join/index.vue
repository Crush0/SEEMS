<template>
  <div class="join-container">
    <span v-if="roleStatus === '待审核'">
      您已申请加入，请等待管理员审核。
    </span>
    <span v-else-if="roleStatus === '禁用'">
      您的账户被管理员禁用，请联系船只管理员解封。
      <a-button type="primary" @click="handleLogout">登出</a-button>
    </span>

    <a-form
      v-else
      ref="form"
      class="join-form"
      :model="joinForm"
      :rules="rules"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item label="船舶初始登记号" field="shipNumber">
        <a-input
          v-model="joinForm.shipNumber"
          placeholder="请输入船舶初始登记号"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" style="width: 600px" html-type="submit"
          >提交申请</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
  import { apply2Ship } from '@/api/flow';
  import { useUserStore } from '@/store';
  import { Message } from '@arco-design/web-vue';
  import { useStorage } from '@vueuse/core';
  import { computed } from 'vue';
  import useUser from '@/hooks/user';

  const userStore = useUserStore();
  const roleStatus = computed(() => userStore.status);
  const rules = {
    shipNumber: [{ required: true, message: '请输入船舶初始登记号' }],
  };
  const { logout } = useUser();
  const handleLogout = () => {
    logout();
  };
  const joinForm = useStorage('joinForm', {
    shipNumber: '',
  });

  const handleSubmit = ({ errors, values }) => {
    if (!errors) {
      apply2Ship(values.shipNumber)
        .then(() => {
          Message.success('申请成功，请等待管理员审核');
          localStorage.removeItem('joinForm');
          window.location.reload();
        })
        .catch(() => {
          Message.error('申请失败，请重试');
        });
    }
  };
</script>

<style lang="less" scoped>
  .join-container {
    display: flex;
    justify-content: center;
    .join-form {
      width: 600px;
    }
  }
</style>
