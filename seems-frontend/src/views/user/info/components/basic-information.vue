<template>
  <a-modal
    v-model:visible="editPasswordModalVisible"
    title="修改密码"
    width="500"
    :footer="null"
    :destroy-on-close="true"
    :mask-closable="false"
  >
    <a-form ref="form" :model="formData" :rules="rules">
      <a-form-item label="旧密码" field="oldPassword" validate-trigger="blur">
        <a-input v-model:model-value="formData.oldPassword" type="password" />
      </a-form-item>
      <a-form-item label="新密码" field="newPassword" validate-trigger="blur">
        <a-input v-model:model-value="formData.newPassword" type="password" />
      </a-form-item>
      <a-form-item
        label="确认密码"
        field="confirmPassword"
        validate-trigger="blur"
      >
        <a-input
          v-model:model-value="formData.repeatPassword"
          type="password"
        />
      </a-form-item>
      <a-form-item>
        <a-button
          type="primary"
          html-type="submit"
          @click="confirmEditPassword"
        >
          确认修改
        </a-button>
      </a-form-item>
    </a-form>
  </a-modal>
  <a-descriptions :column="1" size="large" style="margin-left: 20px">
    <a-descriptions-item label="用户名">
      {{ userStore.username }}
    </a-descriptions-item>
    <a-descriptions-item label="邮箱">
      <template v-if="!editEmailInputVisible">
        {{ (userStore.email ?? '') === '' ? '未绑定' : userStore.email }}
        <a-link
          v-if="(userStore.email ?? '') === ''"
          style="color: #1890ff; cursor: pointer"
          @click="editEmailInputVisible = true"
          >绑定</a-link
        >
      </template>
      <template v-else>
        <a-space>
          <a-input
            v-model:modelValue="formData.email"
            placeholder="请输入邮箱"
          />
          <a-button type="primary" @click="handleEditEmail"> 保存 </a-button>
          <a-button type="secondary" @click="editEmailInputVisible = false">
            取消
          </a-button>
        </a-space>
      </template>
    </a-descriptions-item>
    <a-descriptions-item label="手机号">
      <template v-if="!editPhoneInputVisible">
        {{ (userStore.phone ?? '') === '' ? '未绑定' : userStore.phone }}
        <a-link
          v-if="(userStore.phone ?? '') === ''"
          style="color: #1890ff; cursor: pointer"
          @click="editPhoneInputVisible = true"
          >绑定</a-link
        >
      </template>
      <template v-else>
        <a-space>
          <a-input
            v-model:modelValue="formData.phone"
            placeholder="请输入手机号"
          />
          <a-button type="primary" @click="handleEditPhone"> 保存 </a-button>
          <a-button type="secondary" @click="editPhoneInputVisible = false">
            取消
          </a-button>
        </a-space>
      </template>
    </a-descriptions-item>
    <a-descriptions-item label="角色">
      {{ $t(`userInfo.role.${userStore.role}`) }}
    </a-descriptions-item>
  </a-descriptions>
  <a-button
    type="primary"
    style="margin-left: 20px"
    @click="handleEditPassword"
  >
    修改密码
  </a-button>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicInfoModel } from '@/api/user-center';
  import { useUserStore } from '@/store';
  import { Message } from '@arco-design/web-vue';
  import useVisible from '@/hooks/visible';
  import { editPassword } from '@/api/user';
  import useUser from '@/hooks/user';

  const { logout } = useUser();
  const userStore = useUserStore();
  const editEmailInputVisible = ref(false);
  const editPhoneInputVisible = ref(false);
  const {
    visible: editPasswordModalVisible,
    toggle: toggleEditPasswordModalVisible,
  } = useVisible(false);
  const formData = ref<BasicInfoModel>({
    email: userStore.email,
    phone: userStore.phone,
    oldPassword: '',
    newPassword: '',
    repeatPassword: '',
  });
  const rules = {
    oldPassword: [{ required: true, message: '请输入旧密码' }],
    newPassword: [
      { required: true, message: '请输入新密码' },
      { minLength: 6, message: '密码长度至少6位' },
    ],
    repeatPassword: [
      { required: true, message: '请输入确认密码' },
      {
        validator: (value: any, callback: (error?: string) => void) => {
          if (value !== formData.value.newPassword) {
            callback('两次密码输入不一致!');
          }
          callback();
        },
      },
    ],
  };
  const handleEditEmail = () => {
    if (formData.value.email === '') {
      Message.error('邮箱不能为空');
      return;
    }
    if (
      !/^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(
        formData.value.email
      )
    ) {
      Message.error('邮箱格式不正确');
      return;
    }
    userStore
      .updateBasicInfo({
        email: formData.value.email,
      })
      .then(() => {
        Message.success('修改成功');
        editEmailInputVisible.value = false;
      });
  };
  const handleEditPhone = () => {
    if (formData.value.phone === '') {
      Message.error('手机号不能为空');
      return;
    }
    if (!/^1[3,4578][0-9]{9}$/.test(formData.value.phone)) {
      Message.error('手机号格式不正确');
      return;
    }
    userStore
      .updateBasicInfo({
        phone: formData.value.phone,
      })
      .then(() => {
        Message.success('修改成功');
        editPhoneInputVisible.value = false;
      });
  };
  const handleEditPassword = () => {
    toggleEditPasswordModalVisible();
  };
  const confirmEditPassword = () => {
    editPassword({
      oldPassword: formData.value.oldPassword,
      newPassword: formData.value.newPassword,
      repeatPassword: formData.value.repeatPassword,
    }).then(() => {
      Message.success('修改成功');
      toggleEditPasswordModalVisible();
      logout();
    });
  };
</script>

<style scoped lang="less">
  .form {
    width: 540px;
    margin: 0 auto;
  }
</style>
