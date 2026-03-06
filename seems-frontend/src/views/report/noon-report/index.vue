<template>
  <div class="container">
    <EditReportModal
      v-model:visible="visible"
      v-model:model="editForm"
      @on-ok="fetchData"
    />
    <Breadcrumb
      :items="['menu.visualization', 'menu.visualization.noonReport']"
    />
    <a-card class="general-card" title="正午报告">
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="formModel"
            :label-col-props="{ span: 4 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="right"
          >
            <a-row :gutter="64">
              <a-col :span="24">
                <a-form-item field="createdTime" label="生成时间">
                  <a-range-picker
                    v-model="formModel.createdTime"
                    show-time
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="horizontal" :size="18">
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
          <a-button
            type="primary"
            :loading="generateLoading"
            @click="handleGenerateReport"
            >生成报告</a-button
          >
        </a-col>
        <a-col
          :span="12"
          style="display: flex; align-items: center; justify-content: end"
        >
          <!--          <a-button>-->
          <!--            <template #icon>-->
          <!--              <icon-download />-->
          <!--            </template>-->
          <!--            {{ $t('searchTable.operation.download') }}-->
          <!--          </a-button>-->
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
        </a-col>
      </a-row>
      <a-table
        v-model:selectedKeys="rowSelection.selectedRowKeys"
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        :columns="(cloneColumns as TableColumnData[])"
        :data="renderData"
        :stripe="true"
        :bordered="false"
        :size="size"
        :row-selection="rowSelection"
        @page-change="onPageChange"
      >
        <template #operations="{ record }">
          <a-button
            v-permission="['admin', 'operator']"
            type="text"
            size="small"
            @click="handleEditReport(record)"
          >
            编辑
          </a-button>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import useLoading from '@/hooks/loading';
  import { Pagination } from '@/types/global';
  import type {
    TableColumnData,
    TableRowSelection,
  } from '@arco-design/web-vue/es/table/interface';
  import cloneDeep from 'lodash/cloneDeep';
  import {
    generateReport,
    NoonReportParams,
    queryReport,
    Report,
  } from '@/api/report';
  import EditReportModal from '@/views/report/noon-report/components/EditReportModal.vue';
  import moment from 'moment';
  import { Message } from '@arco-design/web-vue';

  type SizeProps = 'mini' | 'small' | 'medium' | 'large';
  type Column = TableColumnData & { checked?: true };
  const editForm = ref<Report>({
    shipId: 0,
    time: '',
    theorySpeed: 0,
    actualSpeed: 0,
    longitude: 0,
    latitude: 0,
    direction: 0,
    slidingRate: 0,
    actualVoyage: 0,
    totalVoyage: 0,
    windDirection: 0.0,
    windSpeed: 0.0,
  });
  const visible = ref(false);
  const generateFormModel = () => {
    return {
      createdTime: [],
    };
  };
  const { loading: generateLoading, setLoading: setGenerateLoading } =
    useLoading(false);
  const { loading, setLoading } = useLoading(true);
  const { t } = useI18n();
  const renderData = ref<Report[]>([]);
  const formModel = ref(generateFormModel());
  const cloneColumns = ref<Column[]>([]);
  const showColumns = ref<Column[]>([]);
  const rowSelection = ref<TableRowSelection>({
    type: 'checkbox',
    selectedRowKeys: [],
    showCheckedAll: true,
  });
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
      title: '生成时间',
      dataIndex: 'time',
      fixed: 'left',
      width: 120,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
    },
    {
      title: '理论航速(knot)',
      dataIndex: 'theorySpeed',
    },
    {
      title: '实际航速(knot)',
      dataIndex: 'actualSpeed',
    },
    {
      title: '航向(°)',
      dataIndex: 'direction',
    },
    {
      title: '船位',
      children: [
        {
          title: '经度',
          dataIndex: 'longitude',
        },
        {
          title: '纬度',
          dataIndex: 'latitude',
        },
      ],
    },
    {
      title: '海况',
      children: [
        {
          title: '风速(m/s)',
          dataIndex: 'windSpeed',
        },
        {
          title: '风向(°)',
          dataIndex: 'windDirection',
        },
      ],
    },
    {
      title: '实际航程(km)',
      dataIndex: 'actualVoyage',
    },
    {
      title: '航程累计(km)',
      dataIndex: 'totalVoyage',
    },
    {
      title: '滑失率(%)',
      dataIndex: 'slidingRate',
    },
    {
      title: t('searchTable.columns.operations'),
      slotName: 'operations',
      fixed: 'right',
      width: 120,
    },
  ]);
  const fetchData = async (
    params: NoonReportParams = { current: 1, pageSize: 20 }
  ) => {
    setLoading(true);
    try {
      const { data } = await queryReport(params);
      renderData.value = data.list;
      pagination.current = params.current;
      pagination.total = data.total;
    } catch (err) {
      Message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  const search = () => {
    fetchData({
      ...basePagination,
      ...formModel.value,
    } as unknown as NoonReportParams);
  };
  const onPageChange = (current: number) => {
    fetchData({ ...basePagination, current });
  };

  fetchData();
  const reset = () => {
    formModel.value = generateFormModel();
  };

  const handleGenerateReport = () => {
    setGenerateLoading(true);
    generateReport()
      .then((resp) => {
        const { data: report } = resp;
        editForm.value = report;
        editForm.value.time = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
        visible.value = true;
      })
      .finally(() => {
        setGenerateLoading(false);
      });
  };

  const handleEditReport = (record) => {
    editForm.value = JSON.parse(JSON.stringify(record));
    visible.value = true;
  };

  const handleSelectDensity = (
    val: string | number | Record<string, any> | undefined
  ) => {
    size.value = val as SizeProps;
  };

  watch(
    () => columns.value,
    (val) => {
      cloneColumns.value = cloneDeep(val);
      cloneColumns.value.forEach((item) => {
        item.checked = true;
      });
      showColumns.value = cloneDeep(cloneColumns.value);
    },
    { deep: true, immediate: true }
  );
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px;
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
