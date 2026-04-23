<template>
  <div class="receiver-selector">
    <div class="receiver-list">
      <div
        v-for="(receiver, index) in receivers"
        :key="index"
        class="receiver-item"
      >
        <a-space>
          <a-tag :color="receiver.receiverType === 'ROLE' ? 'blue' : 'green'">
            {{ receiver.receiverType === 'ROLE' ? '角色组' : '用户' }}
          </a-tag>
          <span>{{ getReceiverName(receiver) }}</span>
          <icon-delete
            class="delete-icon"
            @click="removeReceiver(index)"
          />
        </a-space>
      </div>

      <a-button
        v-if="receivers.length === 0"
        type="dashed"
        long
        @click="showAddModal"
      >
        <template #icon>
          <icon-plus />
        </template>
        添加接收人
      </a-button>
    </div>

    <a-button
      v-if="receivers.length > 0"
      type="dashed"
      long
      style="margin-top: 8px"
      @click="showAddModal"
    >
      <template #icon>
        <icon-plus />
      </template>
      添加接收人
    </a-button>

    <!-- 添加接收人弹窗 -->
    <a-modal
      v-model:visible="addModalVisible"
      title="添加接收人"
      :footer="false"
      @cancel="addModalVisible = false"
    >
      <a-form :model="addForm" layout="vertical">
        <a-form-item label="接收人类型">
          <a-radio-group v-model="addForm.receiverType">
            <a-radio value="ROLE">角色组</a-radio>
            <a-radio value="USER">具体用户</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 角色组选择 -->
          <a-form-item label="选择角色">
            <a-select
              v-model="addForm.receiverRole"
              placeholder="选择角色组"
            >
              <a-option
                v-for="role in ROLES"
                :key="role.value"
                :value="role.value"
              >
                {{ role.label }}
              </a-option>
            </a-select>
          </a-form-item>

        <!-- 用户选择 -->
          <a-form-item label="选择用户">
            <a-select
              v-model="addForm.receiverId"
              placeholder="选择用户"
              multiple
              :options="userOptions"
              :field-names="{ value: 'id', label: 'username' }"
            />
          </a-form-item>

        <a-form-item style="text-align: right">
          <a-space>
            <a-button @click="addModalVisible = false">取消</a-button>
            <a-button type="primary" @click="handleAddReceiver">
              确定
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { ROLES } from '@/api/alarm-strategy';

interface ReceiverConfig {
  receiverType: 'ROLE' | 'USER';
  receiverId?: number;
  receiverRole?: string;
}

interface Props {
  modelValue: ReceiverConfig[];
}

interface Emits {
  (e: 'update:modelValue', value: ReceiverConfig[]): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const receivers = ref<ReceiverConfig[]>([...props.modelValue]);
const addModalVisible = ref(false);

// 添加表单
const addForm = reactive({
  receiverType: 'ROLE' as 'ROLE' | 'USER',
  receiverRole: undefined as string | undefined,
  receiverId: undefined as number[] | undefined,
});

// 模拟用户选项（实际应从API获取）
const userOptions = ref([
  { id: 1, username: 'admin' },
  { id: 2, username: 'operator1' },
  { id: 3, username: 'user1' },
]);

// 获取接收人名称
const getReceiverName = (receiver: ReceiverConfig) => {
  if (receiver.receiverType === 'ROLE') {
    const role = ROLES.find((r) => r.value === receiver.receiverRole);
    return role?.label || receiver.receiverRole || '';
  } else {
    const user = userOptions.value.find((u) => u.id === receiver.receiverId);
    return user?.username || `用户ID: ${receiver.receiverId}`;
  }
};

// 显示添加弹窗
const showAddModal = () => {
  addForm.receiverType = 'ROLE';
  addForm.receiverRole = undefined;
  addForm.receiverId = undefined;
  addModalVisible.value = true;
};

// 添加接收人
const handleAddReceiver = () => {
  if (addForm.receiverType === 'ROLE') {
    // 添加角色组
    if (!addForm.receiverRole) {
      Message.warning('请选择角色组');
      return;
    }

    // 检查是否已存在
    const exists = receivers.value.some(
      (r) =>
        r.receiverType === 'ROLE' && r.receiverRole === addForm.receiverRole
    );
    if (exists) {
      Message.warning('该角色组已存在');
      return;
    }

    receivers.value.push({
      receiverType: 'ROLE',
      receiverRole: addForm.receiverRole,
    });
  } else {
    // 添加用户
    if (!addForm.receiverId || addForm.receiverId.length === 0) {
      Message.warning('请选择用户');
      return;
    }

    // 批量添加用户
    addForm.receiverId.forEach((userId) => {
      const exists = receivers.value.some(
        (r) => r.receiverType === 'USER' && r.receiverId === userId
      );
      if (!exists) {
        receivers.value.push({
          receiverType: 'USER',
          receiverId: userId,
        });
      }
    });
  }

  emit('update:modelValue', receivers.value);
  addModalVisible.value = false;
  Message.success('添加成功');
};

// 移除接收人
const removeReceiver = (index: number) => {
  receivers.value.splice(index, 1);
  emit('update:modelValue', receivers.value);
};

// 监听外部变化
watch(
  () => props.modelValue,
  (val) => {
    receivers.value = [...val];
  },
  { deep: true }
);
</script>

<style scoped lang="less">
.receiver-selector {
  .receiver-list {
    max-height: 200px;
    overflow-y: auto;

    .receiver-item {
      padding: 8px 12px;
      background-color: var(--color-fill-1);
      border-radius: 4px;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .delete-icon {
        cursor: pointer;
        color: var(--color-text-3);
        transition: color 0.2s;

        &:hover {
          color: rgb(var(--danger-6));
        }
      }
    }
  }
}
</style>
