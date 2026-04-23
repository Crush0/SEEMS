<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑策略' : '新增策略'"
    :width="900"
    :footer="false"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
      @submit="handleSubmit"
    >
      <a-row :gutter="16">
        <!-- 基本信息 -->
        <a-col :span="24">
          <a-divider orientation="left">基本信息</a-divider>
        </a-col>
        <a-col :span="12">
          <a-form-item label="船舶" field="shipId">
            <a-select
              v-model="formData.shipId"
              placeholder="选择船舶（空表示全局策略）"
              allow-clear
            >
              <a-option value="">全局策略</a-option>
              <a-option :value="2029764200985022466">拖轮1</a-option>
              <a-option :value="2029764200985022467">拖轮2</a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="报警类型" field="alarmType">
            <a-select v-model="formData.alarmType" placeholder="选择报警类型">
              <a-option
                v-for="type in ALARM_TYPES"
                :key="type.value"
                :value="type.value"
              >
                {{ type.label }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="报警级别" field="alarmLevel">
            <a-select v-model="formData.alarmLevel" placeholder="选择报警级别">
              <a-option
                v-for="level in ALARM_LEVELS"
                :key="level.value"
                :value="level.value"
              >
                {{ level.label }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="优先级" field="priority">
            <a-input-number
              v-model="formData.priority"
              :min="0"
              :max="100"
              placeholder="0-100"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>

        <!-- 触发条件 -->
        <a-col :span="24">
          <a-divider orientation="left">触发条件</a-divider>
        </a-col>
        <a-col :span="12">
          <a-form-item label="触发条件" field="triggerCondition">
            <a-select v-model="formData.triggerCondition" placeholder="选择触发条件">
              <a-option
                v-for="condition in TRIGGER_CONDITIONS"
                :key="condition.value"
                :value="condition.value"
              >
                {{ condition.label }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="阈值1" field="thresholdValue">
            <a-input-number
              v-model="formData.thresholdValue"
              placeholder="输入阈值"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12" v-if="formData.triggerCondition === 'BETWEEN'">
          <a-form-item label="阈值2" field="thresholdValue2">
            <a-input-number
              v-model="formData.thresholdValue2"
              placeholder="输入第二个阈值"
              :min="formData.thresholdValue"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>

        <!-- 触发时机 -->
        <a-col :span="24">
          <a-divider orientation="left">触发时机</a-divider>
        </a-col>
        <a-col :span="12">
          <a-form-item label="触发时机" field="triggerTiming">
            <a-select v-model="formData.triggerTiming" placeholder="选择触发时机">
              <a-option
                v-for="timing in TRIGGER_TIMINGS"
                :key="timing.value"
                :value="timing.value"
              >
                {{ timing.label }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12" v-if="formData.triggerTiming === 'DURATION'">
          <a-form-item label="持续时间（秒）" field="durationSeconds">
            <a-input-number
              v-model="formData.durationSeconds"
              :min="1"
              placeholder="输入持续时间"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>

        <!-- 消息通知 -->
        <a-col :span="24">
          <a-divider orientation="left">消息通知</a-divider>
        </a-col>
        <a-col :span="24">
          <a-form-item label="发送消息" field="enableNotification">
            <a-switch v-model="formData.enableNotification" />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="标题模板" field="titleTemplate">
            <a-textarea
              v-model="formData.titleTemplate"
              placeholder="例如：{ship_name} {alarm_type}报警"
              :auto-size="{ minRows: 2, maxRows: 4 }"
            />
            <template #extra>
              <a-space>
                <span>可用变量：</span>
                <a-tag
                  v-for="v in templateVariables"
                  :key="v"
                  size="small"
                  @click="insertVariable('title', v)"
                  style="cursor: pointer"
                >
                  {{ '{' + v + '}' }}
                </a-tag>
              </a-space>
            </template>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="内容模板" field="contentTemplate">
            <a-textarea
              v-model="formData.contentTemplate"
              placeholder="例如：当前值：{value}，阈值：{threshold}"
              :auto-size="{ minRows: 4, maxRows: 8 }"
            />
            <template #extra>
              <a-space>
                <span>可用变量：</span>
                <a-tag
                  v-for="v in templateVariables"
                  :key="v"
                  size="small"
                  @click="insertVariable('content', v)"
                  style="cursor: pointer"
                >
                  {{ '{' + v + '}' }}
                </a-tag>
              </a-space>
            </template>
          </a-form-item>
        </a-col>

        <!-- 接收人配置 -->
        <a-col :span="24">
          <a-divider orientation="left">接收人配置</a-divider>
        </a-col>
        <a-col :span="24">
          <ReceiverSelector v-model="formData.receivers" />
        </a-col>
      </a-row>

      <a-form-item style="margin-top: 20px; text-align: right">
        <a-space>
          <a-button @click="handleCancel">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="submitting">
            {{ isEdit ? '保存' : '创建' }}
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  createStrategy,
  updateStrategy,
  getStrategyDetail,
  ALARM_TYPES,
  ALARM_LEVELS,
  TRIGGER_CONDITIONS,
  TRIGGER_TIMINGS,
  type CreateAlarmStrategyRequest,
  type ReceiverConfig,
} from '@/api/alarm-strategy';
import ReceiverSelector from './receiver-selector.vue';

interface Props {
  visible: boolean;
  isEdit: boolean;
  strategyId?: number;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'updateOk'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const formRef = ref();
const submitting = ref(false);

// 表单数据
const formData = reactive<CreateAlarmStrategyRequest>({
  shipId: undefined,
  alarmType: '',
  alarmLevel: '',
  triggerCondition: 'LESS_THAN',
  thresholdValue: 0,
  thresholdValue2: undefined,
  triggerTiming: 'IMMEDIATE',
  durationSeconds: undefined,
  enableNotification: true,
  titleTemplate: '',
  contentTemplate: '',
  priority: 0,
  receivers: [],
});

// 表单验证规则
const formRules = {
  alarmType: [{ required: true, message: '请选择报警类型' }],
  alarmLevel: [{ required: true, message: '请选择报警级别' }],
  triggerCondition: [{ required: true, message: '请选择触发条件' }],
  thresholdValue: [{ required: true, message: '请输入阈值' }],
  triggerTiming: [{ required: true, message: '请选择触发时机' }],
  titleTemplate: [{ required: true, message: '请输入标题模板' }],
  contentTemplate: [{ required: true, message: '请输入内容模板' }],
  receivers: [
    {
      required: true,
      validator: (value: ReceiverConfig[]) => {
        return value && value.length > 0;
      },
      message: '请至少添加一个接收人',
    },
  ],
};

// 模板变量
const templateVariables = [
  'ship_name',
  'alarm_type',
  'value',
  'threshold',
  'alarm_time',
  'battery_position',
  'suggestion',
];

// 插入变量
const insertVariable = (field: 'title' | 'content', variable: string) => {
  const text = `{${variable}}`;
  if (field === 'title') {
    formData.titleTemplate += text;
  } else {
    formData.contentTemplate += text;
  }
};

// 加载策略详情
const loadStrategyDetail = async () => {
  if (!props.strategyId) return;

  try {
    const { data } = await getStrategyDetail(props.strategyId);

    // 填充表单
    formData.shipId = data.shipId;
    formData.alarmType = data.alarmType;
    formData.alarmLevel = data.alarmLevel;
    formData.triggerCondition = data.triggerCondition;
    formData.thresholdValue = data.thresholdValue;
    formData.thresholdValue2 = data.thresholdValue2;
    formData.triggerTiming = data.triggerTiming;
    formData.durationSeconds = data.durationSeconds;
    formData.enableNotification = data.enableNotification;
    formData.titleTemplate = data.titleTemplate;
    formData.contentTemplate = data.contentTemplate;
    formData.priority = data.priority;
    formData.receivers = data.receivers.map((r) => ({
      receiverType: r.receiverType,
      receiverId: r.receiverId,
      receiverRole: r.receiverRole,
    }));
  } catch (error) {
    Message.error('加载策略详情失败');
  }
};

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate();
  if (!valid) return;

  submitting.value = true;
  try {
    if (props.isEdit) {
      await updateStrategy({
        ...formData,
        id: props.strategyId!,
      });
      Message.success('更新成功');
    } else {
      await createStrategy(formData);
      Message.success('创建成功');
    }

    emit('updateOk');
    handleCancel();
  } catch (error) {
    Message.error(props.isEdit ? '更新失败' : '创建失败');
  } finally {
    submitting.value = false;
  }
};

// 取消
const handleCancel = () => {
  formRef.value?.resetFields();
  emit('update:visible', false);
};

// 监听弹窗打开
watch(
  () => props.visible,
  (val) => {
    if (val && props.isEdit) {
      loadStrategyDetail();
    } else if (val && !props.isEdit) {
      // 重置表单
      formRef.value?.resetFields();
      // 设置默认值
      formData.receivers = [];
      formData.enableNotification = true;
      formData.priority = 0;
      formData.triggerCondition = 'LESS_THAN';
      formData.triggerTiming = 'IMMEDIATE';
    }
  }
);
</script>

<style scoped lang="less">
:deep(.arco-divider-text) {
  font-weight: 500;
  font-size: 14px;
}
</style>
