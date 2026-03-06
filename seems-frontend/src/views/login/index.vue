<template>
  <div class="container">
    <div class="logo">
      <img
        alt="logo"
        src="/src/assets/images/logo/trans-white.png"
        width="33"
        height="33"
      />
      <div class="logo-text">SEES</div>
    </div>
    <LoginBanner />
    <div class="content">
      <div class="content-inner">
        <Transition mode="out-in">
          <LoginForm v-if="isLoginForm" @handle-change="switchForm" />
          <RegisterForm v-else @handle-change="switchForm" />
        </Transition>
      </div>
      <div class="footer">
        <Footer />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import Footer from '@/components/footer/index.vue';
  import { onBeforeMount, ref } from 'vue';
  import { useUserStore } from '@/store';
  import { isLogin } from '@/utils/auth';
  import { useRouter } from 'vue-router';
  import RegisterForm from './components/register-form.vue';
  import LoginForm from './components/login-form.vue';
  import LoginBanner from './components/banner.vue';

  const isLoginForm = ref(true);
  const switchForm = () => {
    isLoginForm.value = !isLoginForm.value;
  };
  const userStore = useUserStore();
  const router = useRouter();
  onBeforeMount(() => {
    if (isLogin() && userStore.role) {
      router.push({
        name: 'Workplace',
      });
    }
  });
</script>

<style lang="less" scoped>
  .container {
    display: flex;
    height: 100vh;

    .banner {
      width: 550px;
      background: linear-gradient(163.85deg, #1d2129 0%, #00308f 100%);
    }

    .content {
      position: relative;
      display: flex;
      flex: 1;
      align-items: center;
      justify-content: center;
      padding-bottom: 40px;
    }

    .footer {
      position: absolute;
      right: 0;
      bottom: 0;
      width: 100%;
    }
  }

  .logo {
    position: fixed;
    top: 24px;
    left: 22px;
    z-index: 1;
    display: inline-flex;
    align-items: center;

    &-text {
      margin-right: 4px;
      margin-left: 4px;
      color: var(--color-fill-1);
      font-size: 20px;
    }
  }
</style>

<style lang="less" scoped>
  // responsive
  @media (max-width: @screen-lg) {
    .container {
      .banner {
        width: 25%;
      }
    }
  }
</style>
