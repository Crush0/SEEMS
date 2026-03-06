<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '17px 20px 21px 20px' }"
    >
      <template #title>
        {{ $t('workplace.noonReport') }}
      </template>
      <template #extra>
        <a-link @click="$router.push({ name: 'NoonReport' })">{{
          $t('workplace.viewMore')
        }}</a-link>
      </template>
      <a-space direction="vertical" :size="10" fill>
        <a-table
          :columns="columns"
          :data="renderList"
          :pagination="false"
          :bordered="false"
          :scroll="{ x: '1280px', y: '264px' }"
        >
        </a-table>
      </a-space>
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import type {
    TableColumnData,
    TableData,
  } from '@arco-design/web-vue/es/table/interface';
  import { queryReport } from '@/api/report';

  const { loading, setLoading } = useLoading();
  const renderList = ref<TableData[]>();
  const fetchData = async () => {
    try {
      setLoading(true);
      const { data } = await queryReport({
        current: 1,
        pageSize: 5,
      });
      renderList.value = data.list;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };
  const columns = computed<TableColumnData[]>(() => [
    {
      title: '生成时间',
      dataIndex: 'time',
      fixed: 'left',
      width: 120,
    },
    {
      title: '理论航速',
      dataIndex: 'theorySpeed',
    },
    {
      title: '实际航速',
      dataIndex: 'actualSpeed',
    },
    {
      title: '航向',
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
          title: '风速',
          dataIndex: 'windSpeed',
        },
        {
          title: '风向',
          dataIndex: 'windDirection',
        },
      ],
    },
    {
      title: '实际航程',
      dataIndex: 'actualVoyage',
    },
    {
      title: '航程累计',
      dataIndex: 'totalVoyage',
    },
    {
      title: '滑失率',
      dataIndex: 'slidingRate',
    },
  ]);
  fetchData();
</script>

<style scoped lang="less">
  .general-card {
    min-height: 395px;
  }
  :deep(.arco-table-tr) {
    height: 44px;
    .arco-typography {
      margin-bottom: 0;
    }
  }
  .increases-cell {
    display: flex;
    align-items: center;
    span {
      margin-right: 4px;
    }
  }
</style>
