<template>
  <a-modal v-model:visible="visible">
    <template #title>
      {{ isEdit ? '编辑' : '新增' }}
    </template>
    <a-form ref="form" :model="fieldValue" :rules="rules">
      <a-form-item label="用户名" field="name">
        <a-input
          v-model="fieldValue.name"
          placeholder="请输入用户名"
          :disabled="isEdit"
        />
      </a-form-item>
      <a-form-item label="邮箱" field="email">
        <a-input v-model="fieldValue.email" placeholder="请输入邮箱" />
      </a-form-item>
      <a-form-item label="手机号" field="phone">
        <a-input v-model="fieldValue.phone" placeholder="请输入手机号" />
      </a-form-item>
      <a-form-item label="权限等级" field="role">
        <a-select
          v-model="fieldValue.role"
          :disabled="userStore.username === fieldValue.name"
        >
          <a-option>管理员</a-option>
          <a-option>操作员</a-option>
          <a-option>普通用户</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态" field="status">
        <a-select
          v-model="fieldValue.status"
          :disabled="userStore.username === fieldValue.name"
        >
          <a-option>正常</a-option>
          <a-option>禁用</a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="dashed" @click="visible = false">{{
        isEdit ? '取消' : '关闭'
      }}</a-button>
      <a-popconfirm
        v-permission="['admin']"
        content="你确定要重置该用户密码吗？"
        type="error"
        @ok="handleResetPassword()"
      >
        <a-button
          v-if="isEdit"
          v-permission="['admin']"
          type="secondary"
          status="danger"
          :disabled="userStore.username === fieldValue.name"
          >重置用户密码</a-button
        >
      </a-popconfirm>

      <a-button
        v-permission="['admin']"
        type="primary"
        :loading="loading"
        @click="handleSubmit()"
        >{{ isEdit ? '保存' : '新增' }}</a-button
      >
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { PersonnelRecord, savePersonnel } from '@/api/personnel';
  import { resetPassword } from '@/api/user';
  import { useUserStore } from '@/store';
  import { Message } from '@arco-design/web-vue';
  import { ref } from 'vue';

  const loading = ref(false);
  const visible = defineModel<boolean>('visible');
  const userStore = useUserStore();
  defineProps({
    isEdit: {
      type: Boolean,
      default: false,
    },
  });
  const fieldValue = ref({
    name: '',
    email: '',
    phone: '',
    role: '',
    status: '',
  } as unknown as PersonnelRecord);
  const handleResetPassword = () => {
    resetPassword(fieldValue.value.name)
      .then(() => {
        Message.success('重置密码成功, 新密码为123456');
      })
      .catch(() => {
        Message.error('重置密码失败');
      });
  };
  const form = ref<any>();

  const rules = {
    name: [
      {
        required: true,
        message: '请输入用户名',
        trigger: 'blur',
      },
    ],
  };
  const setFieldsValue = (value: any) => {
    if (value) {
      fieldValue.value = {
        ...value,
      };
    } else {
      fieldValue.value = {
        name: '',
        email: '',
        phone: '',
        role: '普通用户',
        status: '正常',
      };
    }
  };
  const emit = defineEmits(['update-ok']);
  const handleSubmit = () => {
    form.value?.validate((errors) => {
      if (errors) return;

      savePersonnel(fieldValue.value)
        .then(() => {
          visible.value = false;
          Message.success('操作成功');
          // eslint-disable-next-line vue/custom-event-name-casing
          emit('update-ok');
        })
        .catch(() => {
          Message.error('操作失败');
        });
    });
  };
  defineExpose({
    setFieldsValue,
  });
</script>

<style scoped></style>
