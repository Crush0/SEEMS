<template>
  <div class="register-form-wrapper">
    <div class="register-form-title">注册新账户</div>
    <div class="register-form-sub-title">让我们开始吧</div>
    <div class="register-form-error-msg">{{ errorMessage }}</div>
    <a-form
      ref="loginForm"
      :model="userInfo"
      class="login-form"
      layout="vertical"
      @submit="handleSubmit"
    >
      <a-form-item
        field="username"
        :rules="[{ required: true, message: $t('login.form.userName.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input
          v-model="userInfo.username"
          :placeholder="$t('login.form.userName.placeholder')"
        >
          <template #prefix>
            <icon-user />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        field="password"
        :rules="[{ required: true, message: $t('login.form.password.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input-password
          v-model="userInfo.password"
          :placeholder="$t('login.form.password.placeholder')"
          allow-clear
        >
          <template #prefix>
            <icon-lock />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item
        field="repeatPassword"
        :rules="[
          { required: true, message: $t('login.form.password.errMsg') },
          { validator: validateRepeatPassword },
        ]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input-password
          v-model="userInfo.repeatPassword"
          placeholder="确认密码"
          allow-clear
        >
          <template #prefix>
            <icon-lock />
          </template>
        </a-input-password>
      </a-form-item>
      <a-space :size="16" direction="vertical">
        <a-button type="primary" html-type="submit" long :loading="loading">
          注册
        </a-button>
        <a-button
          type="text"
          long
          class="login-form-register-btn"
          @click="$emit('handleChange')"
        >
          已有账号？登录
        </a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
  import { register } from '@/api/user';
  import { Message } from '@arco-design/web-vue';
  import { reactive, ref } from 'vue';

  const errorMessage = ref('');
  const loading = ref(false);
  const registerConfig = ref({
    username: '',
    password: '',
    repeatPassword: '',
  });
  const userInfo = reactive({
    username: registerConfig.value.username,
    password: registerConfig.value.password,
    repeatPassword: registerConfig.value.repeatPassword,
  });
  const validateRepeatPassword = (value: string, callback: any) => {
    if (value !== userInfo.password) {
      callback('两次输入的密码不一致!');
    } else {
      callback();
    }
  };
  const emit = defineEmits(['handleChange']);

  const handleSubmit = ({ errors }) => {
    if (!errors) {
      loading.value = true;
      register(userInfo)
        .then(() => {
          loading.value = false;
          errorMessage.value = '';
          Message.success('注册成功，请登录');
          // eslint-disable-next-line vue/custom-event-name-casing
          emit('handleChange');
        })
        .catch((err) => {
          loading.value = false;
          errorMessage.value = err.message;
        });
    }
  };
</script>

<style lang="less" scoped>
  .register-form {
    &-wrapper {
      width: 320px;
    }

    &-title {
      color: var(--color-text-1);
      font-weight: 500;
      font-size: 24px;
      line-height: 32px;
    }

    &-sub-title {
      color: var(--color-text-3);
      font-size: 16px;
      line-height: 24px;
    }

    &-error-msg {
      height: 32px;
      color: rgb(var(--red-6));
      line-height: 32px;
    }

    &-password-actions {
      display: flex;
      justify-content: space-between;
    }

    &-register-btn {
      color: var(--color-text-3) !important;
    }
  }
</style>
