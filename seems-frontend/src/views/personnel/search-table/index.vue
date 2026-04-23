<template>
  <div class="container">
    <EditModal
      ref="editModal"
      v-model:visible="editModalVisible"
      :is-edit="isEdit"
      @update-ok="search"
    />
    <Breadcrumb :items="['menu.personnel', 'menu.personnel.searchTable']" />
    <a-card class="general-card" title="人员管理 / 查询">
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="formModel"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="name" label="用户名">
                  <a-input v-model="formModel.name" placeholder="输入用户名" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="email" label="邮箱">
                  <a-input v-model="formModel.email" placeholder="输入邮箱" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="phone" label="手机号">
                  <a-input v-model="formModel.phone" placeholder="输入手机号" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="role" label="权限">
                  <a-select
                    v-model="formModel.role"
                    :options="filterRoleOptions"
                    placeholder="全部"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <!-- <a-form-item
                  field="createdTime"
                  :label="$t('searchTable.form.createdTime')"
                >
                  <a-range-picker
                    v-model="formModel.createdTime"
                    style="width: 100%"
                  />
                </a-form-item> -->
              </a-col>
              <a-col :span="8">
                <a-form-item
                  field="status"
                  :label="$t('searchTable.form.status')"
                >
                  <a-select
                    v-model="formModel.status"
                    :options="statusOptions"
                    :placeholder="$t('searchTable.form.selectDefault')"
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
              {{ $t('searchTable.form.search') }}
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              {{ $t('searchTable.form.reset') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 0" />
      <a-row style="margin-bottom: 16px">
        <a-col :span="12">
          <a-space>
            <a-button type="primary" @click="create">
              <template #icon>
                <icon-plus />
              </template>
              {{ $t('searchTable.operation.create') }}
            </a-button>
            <!-- <a-upload action="/">
              <template #upload-button>
                <a-button>
                  {{ $t('searchTable.operation.import') }}
                </a-button>
              </template>
            </a-upload> -->
          </a-space>
        </a-col>
        <a-col
          :span="12"
          style="display: flex; align-items: center; justify-content: end"
        >
          <!-- <a-button>
            <template #icon>
              <icon-download />
            </template>
            {{ $t('searchTable.operation.download') }}
          </a-button> -->
          <a-tooltip :content="$t('searchTable.actions.refresh')">
            <div class="action-icon" @click="search"
              ><icon-refresh size="18"
            /></div>
          </a-tooltip>
          <a-dropdown @select="handleSelectDensity">
            <a-tooltip :content="$t('searchTable.actions.density')">
              <div class="action-icon"><icon-line-height size="18" /></div>
            </a-tooltip>
            <template #content>
              <a-doption
                v-for="item in densityList"
                :key="item.value"
                :value="item.value"
                :class="{ active: item.value === size }"
              >
                <span>{{ item.name }}</span>
              </a-doption>
            </template>
          </a-dropdown>
          <a-tooltip :content="$t('searchTable.actions.columnSetting')">
            <a-popover
              trigger="click"
              position="bl"
              @popup-visible-change="popupVisibleChange"
            >
              <div class="action-icon"><icon-settings size="18" /></div>
              <template #content>
                <div id="tableSetting">
                  <div
                    v-for="(item, index) in showColumns"
                    :key="item.dataIndex"
                    class="setting"
                  >
                    <div style="margin-right: 4px; cursor: move">
                      <icon-drag-arrow />
                    </div>
                    <div>
                      <a-checkbox
                        v-model="item.checked"
                        @change="
                          handleChange($event, item as TableColumnData, index)
                        "
                      >
                      </a-checkbox>
                    </div>
                    <div class="title">
                      {{ item.title === '#' ? '序列号' : item.title }}
                    </div>
                  </div>
                </div>
              </template>
            </a-popover>
          </a-tooltip>
        </a-col>
      </a-row>
      <a-table
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        :columns="(cloneColumns as TableColumnData[])"
        :data="renderData"
        :bordered="false"
        :size="size"
        @page-change="onPageChange"
      >
        <template #index="{ rowIndex }">
          {{ rowIndex + 1 + (pagination.current - 1) * pagination.pageSize }}
        </template>
        <template #email="{ record }">
          {{
            record.email ? (record.email == '' ? '暂无' : record.email) : '暂无'
          }}
        </template>
        <template #phone="{ record }">
          {{
            record.phone ? (record.phone == '' ? '暂无' : record.phone) : '暂无'
          }}
        </template>
        <template #role="{ record }">
          <a-tag v-if="record.role === 'ADMIN'" color="red">{{
            $t(`personnel.role.${record.role}`)
          }}</a-tag>
          <a-tag v-else-if="record.role === 'OPERATOR'" color="blue">{{
            $t(`personnel.role.${record.role}`)
          }}</a-tag>
          <a-tag v-else>{{ $t(`personnel.role.${record.role}`) }}</a-tag>
        </template>
        <template #status="{ record }">
          <div style="display: inline-block; transform: translateY(-2px)"
            ><span v-if="record.status === 'PENDING'" class="circle"></span>
            <span
              v-else-if="record.status === 'NORMAL'"
              class="circle pass"
            ></span>
            <span v-else class="circle fail"></span
          ></div>
          {{ $t(`personnel.status.${record.status}`) }}
        </template>
        <template #operations="{ record }">
          <a-button
            v-permission="['admin']"
            type="text"
            size="small"
            @click="edit(record)"
          >
            编辑
          </a-button>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, nextTick, reactive, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import useLoading from '@/hooks/loading';
  import { Pagination } from '@/types/global';
  import type { SelectOptionData } from '@arco-design/web-vue/es/select/interface';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import cloneDeep from 'lodash/cloneDeep';
  import Sortable from 'sortablejs';
  import {
    PersonnelRecord,
    queryPersonnel,
    queryPersonnelForm,
  } from '@/api/personnel';
  import { removeEmptyStrings } from '@/utils';
  import EditModal from '../components/EditModal.vue';

  type SizeProps = 'mini' | 'small' | 'medium' | 'large';
  type Column = TableColumnData & { checked?: true };
  const editModal = ref<any>();
  const editModalVisible = ref(false);
  const isEdit = ref(false);
  const edit = (value) => {
    editModalVisible.value = true;
    isEdit.value = true;
    editModal.value.setFieldsValue(value);
  };
  const create = () => {
    editModalVisible.value = true;
    isEdit.value = false;
    editModal.value.setFieldsValue(null);
  };
  const generateFormModel = () => {
    return {
      name: '',
      email: '',
      phone: '',
      role: '',
      status: '',
    };
  };
  const { loading, setLoading } = useLoading(true);
  const { t } = useI18n();
  const renderData = ref<PersonnelRecord[]>([]);
  const formModel = ref(generateFormModel());
  const cloneColumns = ref<Column[]>([]);
  const showColumns = ref<Column[]>([]);

  const size = ref<SizeProps>('medium');

  const basePagination: Pagination = {
    current: 1,
    pageSize: 20,
  };
  const pagination = reactive({
    ...basePagination,
  });
  const densityList = computed(() => [
    {
      name: t('searchTable.size.mini'),
      value: 'mini',
    },
    {
      name: t('searchTable.size.small'),
      value: 'small',
    },
    {
      name: t('searchTable.size.medium'),
      value: 'medium',
    },
    {
      name: t('searchTable.size.large'),
      value: 'large',
    },
  ]);
  const columns = computed<TableColumnData[]>(() => [
    {
      title: '序号',
      dataIndex: 'index',
      slotName: 'index',
    },
    {
      title: '用户名',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      slotName: 'email',
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      slotName: 'phone',
    },
    {
      title: '权限',
      dataIndex: 'role',
      slotName: 'role',
    },
    {
      title: '状态',
      dataIndex: 'status',
      slotName: 'status',
    },
    {
      title: t('searchTable.columns.operations'),
      dataIndex: 'operations',
      slotName: 'operations',
    },
  ]);

  const filterRoleOptions = computed<SelectOptionData[]>(() => [
    {
      label: '管理员',
      value: 'ADMIN',
    },
    {
      label: '操作员',
      value: 'OPERATOR',
    },
    {
      label: '普通用户',
      value: 'USER',
    },
  ]);
  const statusOptions = computed<SelectOptionData[]>(() => [
    {
      label: '正常',
      value: 'NORMAL',
    },
    {
      label: '待审核',
      value: 'PENDING',
    },
    {
      label: '禁用',
      value: 'DISABLED',
    },
  ]);
  const fetchData = async (
    params: queryPersonnelForm = { current: 1, pageSize: 20 }
  ) => {
    setLoading(true);
    try {
      const { data } = await queryPersonnel(params);
      renderData.value = data.list.filter(Boolean);
      pagination.current = params.current;
      pagination.total = data.total;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };

  const search = () => {
    fetchData({
      ...basePagination,
      ...removeEmptyStrings(formModel.value),
    } as unknown as queryPersonnelForm);
  };
  const onPageChange = (current: number) => {
    fetchData({ ...basePagination, current });
  };

  fetchData();
  const reset = () => {
    formModel.value = generateFormModel();
  };

  const handleSelectDensity = (
    val: string | number | Record<string, any> | undefined,
    _e: Event
  ) => {
    size.value = val as SizeProps;
  };

  const handleChange = (
    checked: boolean | (string | boolean | number)[],
    column: Column,
    index: number
  ) => {
    if (!checked) {
      cloneColumns.value = showColumns.value.filter(
        (item) => item.dataIndex !== column.dataIndex
      );
    } else {
      cloneColumns.value.splice(index, 0, column);
    }
  };

  const exchangeArray = <T extends Array<any>>(
    array: T,
    beforeIdx: number,
    newIdx: number,
    isDeep = false
  ): T => {
    const newArray = isDeep ? cloneDeep(array) : array;
    if (beforeIdx > -1 && newIdx > -1) {
      // 先替换后面的，然后拿到替换的结果替换前面的
      newArray.splice(
        beforeIdx,
        1,
        newArray.splice(newIdx, 1, newArray[beforeIdx]).pop()
      );
    }
    return newArray;
  };

  const popupVisibleChange = (val: boolean) => {
    if (val) {
      nextTick(() => {
        const el = document.getElementById('tableSetting') as HTMLElement;
        const sortable = new Sortable(el, {
          onEnd(e: any) {
            const { oldIndex, newIndex } = e;
            exchangeArray(cloneColumns.value, oldIndex, newIndex);
            exchangeArray(showColumns.value, oldIndex, newIndex);
          },
        });
      });
    }
  };

  watch(
    () => columns.value,
    (val) => {
      cloneColumns.value = cloneDeep(val);
      cloneColumns.value.forEach((item, index) => {
        item.checked = true;
      });
      showColumns.value = cloneDeep(cloneColumns.value);
    },
    { deep: true, immediate: true }
  );
</script>

<script lang="ts">
  export default {
    name: 'SearchTable',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }
  :deep(.arco-table-th) {
    &:last-child {
      .arco-table-th-item-title {
        margin-left: 16px;
      }
    }
  }
  .action-icon {
    margin-left: 12px;
    cursor: pointer;
  }
  .active {
    color: #0960bd;
    background-color: #e3f4fc;
  }
  .setting {
    display: flex;
    align-items: center;
    width: 200px;
    .title {
      margin-left: 12px;
      cursor: pointer;
    }
  }
</style>
