<template>
  <div class="container">
    <StrategyModal
      ref="strategyModal"
      v-model:visible="modalVisible"
      :is-edit="isEdit"
      :strategy-id="currentStrategyId"
      @update-ok="fetchData"
    />

    <Breadcrumb :items="['menu.alarm', 'menu.alarm.strategy']" />

    <a-card class="general-card" title="报警策略管理">
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="queryForm"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="shipId" label="船舶">
                  <a-select
                    v-model="queryForm.shipId"
                    :options="shipOptions"
                    placeholder="全部"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="alarmType" label="报警类型">
                  <a-select
                    v-model="queryForm.alarmType"
                    :options="alarmTypes"
                    placeholder="全部"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="isEnabled" label="状态">
                  <a-select
                    v-model="queryForm.isEnabled"
                    :options="statusOptions"
                    placeholder="全部"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
            <a-button type="primary" @click="search">
              <template #icon>
                <icon-search />
              </template>
              查询
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              重置
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 0" />
      <a-row style="margin-bottom: 16px">
        <a-col :span="12">
          <a-space>
            <a-button type="primary" @click="createStrategy">
              <template #icon>
                <icon-plus />
              </template>
              新增策略
            </a-button>
          </a-space>
        </a-col>
        <a-col
          :span="12"
          style="display: flex; align-items: center; justify-content: end"
        >
          <a-tooltip content="刷新">
            <div class="action-icon" @click="fetchData">
              <icon-refresh size="18" />
            </div>
          </a-tooltip>
        </a-col>
      </a-row>
      <a-table
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #columns>
          <a-table-column title="船舶" data-index="shipName" />
          <a-table-column title="报警类型" data-index="alarmTypeName" />
          <a-table-column title="报警级别" data-index="alarmLevelName">
            <template #cell="{ record }">
              <a-tag :color="getLevelColor(record.alarmLevel)">
                {{ record.alarmLevelName }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="触发条件" data-index="triggerCondition">
            <template #cell="{ record }">
              {{ getTriggerConditionText(record) }}
            </template>
          </a-table-column>
          <a-table-column title="触发时机" data-index="triggerTiming">
            <template #cell="{ record }">
              {{ getTimingText(record) }}
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="isEnabled">
            <template #cell="{ record }">
              <a-tag :color="record.isEnabled ? 'green' : 'gray'">
                {{ record.isEnabled ? '启用' : '禁用' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="优先级" data-index="priority" />
          <a-table-column title="创建时间" data-index="createDate" />
          <a-table-column title="操作" fixed="right" :width="200">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="editStrategy(record)">
                  编辑
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  @click="toggleStrategy(record)"
                >
                  {{ record.isEnabled ? '禁用' : '启用' }}
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="deleteStrategy(record)"
                >
                  删除
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { Message, Modal } from '@arco-design/web-vue';
import {
  queryStrategyList,
  deleteStrategy as deleteStrategyApi,
  enableStrategy,
  ALARM_TYPES,
  TRIGGER_CONDITIONS,
  TRIGGER_TIMINGS,
  type AlarmStrategyVO,
} from '@/api/alarm-strategy';
import StrategyModal from './components/strategy-modal.vue';

// 表单数据
const queryForm = reactive({
  shipId: undefined as number | undefined,
  alarmType: undefined as string | undefined,
  isEnabled: undefined as boolean | undefined,
});

// 表格数据
const tableData = ref<AlarmStrategyVO[]>([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 模态框
const modalVisible = ref(false);
const isEdit = ref(false);
const currentStrategyId = ref<number>();

// 选项
const shipOptions = ref([
  { label: '全局策略', value: null },
  { label: '拖轮1', value: 2029764200985022466 },
  { label: '拖轮2', value: 2029764200985022467 },
]);

const alarmTypes = [
  { label: '全部', value: undefined },
  ...ALARM_TYPES.map((t) => ({ label: t.label, value: t.value })),
];

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '启用', value: true },
  { label: '禁用', value: false },
];

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const { data } = await queryStrategyList({
      shipId: queryForm.shipId,
      alarmType: queryForm.alarmType,
      isEnabled: queryForm.isEnabled,
      current: pagination.current,
      pageSize: pagination.pageSize,
    });
    tableData.value = data.records || [];
    pagination.total = data.total || 0;
  } catch (error) {
    Message.error('查询失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const search = () => {
  pagination.current = 1;
  fetchData();
};

// 重置
const reset = () => {
  queryForm.shipId = undefined;
  queryForm.alarmType = undefined;
  queryForm.isEnabled = undefined;
  search();
};

// 新增策略
const createStrategy = () => {
  isEdit.value = false;
  currentStrategyId.value = undefined;
  modalVisible.value = true;
};

// 编辑策略
const editStrategy = (record: AlarmStrategyVO) => {
  isEdit.value = true;
  currentStrategyId.value = record.id;
  modalVisible.value = true;
};

// 切换策略状态
const toggleStrategy = async (record: AlarmStrategyVO) => {
  try {
    await enableStrategy(record.id, !record.isEnabled);
    Message.success(`${record.isEnabled ? '禁用' : '启用'}成功`);
    fetchData();
  } catch (error) {
    Message.error('操作失败');
  }
};

// 删除策略
const deleteStrategy = async (record: AlarmStrategyVO) => {
  // 确认对话框
  const confirmed = await Modal.confirm({
    title: '确认删除',
    content: `确定要删除策略"${record.alarmTypeName}"吗？`,
  });
  if (!confirmed) return;

  try {
    await deleteStrategyApi(record.id);
    Message.success('删除成功');
    fetchData();
  } catch (error) {
    Message.error('删除失败');
  }
};

// 分页变化
const onPageChange = (page: number) => {
  pagination.current = page;
  fetchData();
};

const onPageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize;
  pagination.current = 1;
  fetchData();
};

// 辅助函数
const getLevelColor = (level: string) => {
  const colors: Record<string, string> = {
    INFO: 'blue',
    WARNING: 'orange',
    ERROR: 'red',
    CRITICAL: 'red',
  };
  return colors[level] || 'gray';
};

const getTriggerConditionText = (record: AlarmStrategyVO) => {
  const condition = TRIGGER_CONDITIONS.find(
    (c) => c.value === record.triggerCondition
  );
  const value = record.thresholdValue;
  const value2 = record.thresholdValue2;

  if (record.triggerCondition === 'BETWEEN') {
    return `${condition?.label} ${value} - ${value2}`;
  }
  return `${condition?.label} ${value}`;
};

const getTimingText = (record: AlarmStrategyVO) => {
  const timing = TRIGGER_TIMINGS.find((t) => t.value === record.triggerTiming);
  if (record.triggerTiming === 'DURATION') {
    return `${timing?.label} (${record.durationSeconds}秒)`;
  }
  return timing?.label || '';
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}

.action-icon {
  margin-left: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background-color: var(--color-fill-2);
  }
}
</style>
