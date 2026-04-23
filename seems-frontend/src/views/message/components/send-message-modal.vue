<template>
  <a-modal
    :visible="visible"
    :title="$t('message.sendModal.title')"
    :width="600"
    :ok-loading="submitLoading"
    @before-ok="handleSubmit"
    @cancel="handleCancel"
    @update:visible="emit('update:visible', $event)"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item :label="$t('message.sendModal.sendType')" required>
        <a-radio-group v-model="sendType" @change="handleSendTypeChange">
          <a-radio value="user">{{ $t('message.sendModal.sendToUser') }}</a-radio>
          <a-radio value="role">{{ $t('message.sendModal.sendToRole') }}</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 指定用户 -->
      <a-form-item
        v-if="sendType === 'user'"
        :label="$t('message.sendModal.receiver')"
        required
        field="receiverId"
        :rules="[{ required: true, message: $t('message.sendModal.receiverRequired') }]"
      >
        <a-select
          v-model="form.receiverId"
          :placeholder="$t('message.sendModal.receiverPlaceholder')"
          :loading="userListLoading"
          allow-search
        >
          <a-option v-for="user in userList" :key="user.id" :value="user.id">
            {{ user.name }} ({{ user.email }})
          </a-option>
        </a-select>
      </a-form-item>

      <!-- 角色组 -->
      <a-form-item
        v-if="sendType === 'role'"
        :label="$t('message.sendModal.targetRole')"
        required
        field="role"
        :rules="[{ required: true, message: $t('message.sendModal.roleRequired') }]"
      >
        <a-radio-group v-model="form.role">
          <a-radio value="ADMIN">{{ $t('message.role.ADMIN') }}</a-radio>
          <a-radio value="OPERATOR">{{ $t('message.role.OPERATOR') }}</a-radio>
          <a-radio value="USER">{{ $t('message.role.USER') }}</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item
        :label="$t('message.sendModal.messageTitle')"
        required
        field="title"
        :rules="[{ required: true, message: $t('message.sendModal.titleRequired') }]"
      >
        <a-input
          v-model="form.title"
          :placeholder="$t('message.sendModal.titlePlaceholder')"
          :max-length="200"
          show-word-limit
        />
      </a-form-item>

      <a-form-item
        :label="$t('message.sendModal.content')"
        required
        field="content"
        :rules="[{ required: true, message: $t('message.sendModal.contentRequired') }]"
      >
        <a-textarea
          v-model="form.content"
          :placeholder="$t('message.sendModal.contentPlaceholder')"
          :max-length="2000"
          show-word-limit
          :auto-size="{ minRows: 4, maxRows: 8 }"
        />
      </a-form-item>

      <a-form-item :label="$t('message.sendModal.type')">
        <a-select v-model="form.type" :placeholder="$t('message.sendModal.typePlaceholder')">
          <a-option value="message">{{ $t('message.type.message') }}</a-option>
          <a-option value="notice">{{ $t('message.type.notice') }}</a-option>
          <a-option value="todo">{{ $t('message.type.todo') }}</a-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { Message as ArcoMessage } from '@arco-design/web-vue';
  import { sendMessage, type SendMessageParams } from '@/api/message';
  import { queryPersonnel } from '@/api/personnel';
  import type { PersonnelRecord } from '@/api/personnel';
  import useLoading from '@/hooks/loading';

  interface Props {
    visible: boolean;
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void;
    (e: 'success'): void;
  }

  const props = defineProps<Props>();
  const emit = defineEmits<Emits>();
  const { t } = useI18n();
  const { loading: submitLoading, setLoading: setSubmitLoading } = useLoading(false);

  const sendType = ref<'user' | 'role'>('user');
  const userList = ref<PersonnelRecord[]>([]);
  const userListLoading = ref(false);

  const form = ref({
    receiverId: '',
    role: '' as 'ADMIN' | 'OPERATOR' | 'USER' | '',
    title: '',
    content: '',
    type: 'message' as 'message' | 'notice' | 'todo',
  });

  // 获取用户列表
  const fetchUserList = async () => {
    userListLoading.value = true;
    try {
      const { data } = await queryPersonnel({
        current: 1,
        pageSize: 1000,
      });
      userList.value = data.list;
    } catch (error) {
      // 错误处理
    } finally {
      userListLoading.value = false;
    }
  };

  // 发送类型变化
  const handleSendTypeChange = () => {
    form.value.receiverId = '';
    form.value.role = '';
  };

  // 提交表单
  const handleSubmit = async () => {
    // 验证
    if (sendType.value === 'user' && !form.value.receiverId) {
      return false;
    }
    if (sendType.value === 'role' && !form.value.role) {
      return false;
    }
    if (!form.value.title) {
      return false;
    }
    if (!form.value.content) {
      return false;
    }

    setSubmitLoading(true);
    try {
      const params: SendMessageParams = {
        title: form.value.title,
        content: form.value.content,
        type: form.value.type,
      };

      if (sendType.value === 'user') {
        params.receiverId = form.value.receiverId;
      } else if (form.value.role) {
        params.role = form.value.role;
      }

      await sendMessage(params);
      ArcoMessage.success(t('message.sendModal.success'));
      handleCancel();
      emit('success');
      return true;
    } catch (error) {
      return false;
    } finally {
      setSubmitLoading(false);
    }
  };

  // 取消
  const handleCancel = () => {
    emit('update:visible', false);
    resetForm();
  };

  // 重置表单
  const resetForm = () => {
    form.value = {
      receiverId: '',
      role: '',
      title: '',
      content: '',
      type: 'message',
    };
    sendType.value = 'user';
  };

  // 监听visible变化
  watch(() => props.visible, (newVal) => {
    if (newVal) {
      fetchUserList();
    }
  });
</script>

<style scoped lang="less">
  :deep(.arco-modal-body) {
    max-height: 60vh;
    overflow-y: auto;
  }
</style>
