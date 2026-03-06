<template>
  <div class="create-flow">
    <a-form
      ref="form"
      :model="newShipInfo"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item
        field="name"
        label="船舶名称"
        :rules="[{ required: true, message: '请输入船舶名称' }]"
      >
        <a-input
          v-model="newShipInfo.name"
          placeholder="请输入船舶名称"
        ></a-input>
      </a-form-item>
      <a-form-item
        label="船舶类型"
        field="type"
        :rules="[{ required: true, message: '请输入船舶类型' }]"
      >
        <a-input
          v-model="newShipInfo.type"
          placeholder="请输入船舶类型"
        ></a-input>
      </a-form-item>
      <a-form-item
        label="船舶型号"
        field="model"
        :rules="[{ required: true, message: '请输入船舶型号' }]"
      >
        <a-input
          v-model="newShipInfo.model"
          placeholder="请输入船舶型号"
        ></a-input>
      </a-form-item>
      <a-form-item
        label="船舶初始登记号"
        field="shipOriginNumber"
        :rules="[{ required: true, message: '请输入船舶初始登记号' }]"
      >
        <a-input
          v-model="newShipInfo.shipOriginNumber"
          placeholder="请输入船舶初始登记号"
        ></a-input>
      </a-form-item>
      <a-form-item
        label="船舶拖力 （ton）"
        field="towingForce"
        :rules="[{ required: false, message: '请输入船舶拖力' }]"
      >
        <a-input
          v-model="newShipInfo.towingForce"
          placeholder="请输入船舶拖力"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="maxBatteryCapacity"
        label="船舶最大电池容量 （kwh）"
        tooltip="和能耗预测密切相关，请正确输入"
        :rules="[
          {
            required: true,
            message: '请输入船舶最大电池容量',
          },
        ]"
      >
        <a-input
          v-model="newShipInfo.maxBatteryCapacity"
          placeholder="请输入船舶最大电池容量"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="progress"
        label="船舶吨位 （DWT）"
        :rules="[{ required: true, message: '请输入船舶吨位' }]"
      >
        <a-input
          v-model="newShipInfo.progress"
          placeholder="请输入船舶吨位"
        ></a-input>
      </a-form-item>
      <a-form-item
        label="已航行距离 （KM）"
        field="sailedDistance"
        :rules="[{ required: false, message: '请输入已航行距离' }]"
      >
        <a-input-number
          v-model="newShipInfo.sailedDistance"
          :min="0"
          :step="0.01"
          placeholder="请输入已航行距离"
        ></a-input-number>
      </a-form-item>
      <a-collapse class="extra-info">
        <a-collapse-item key="1" header="船只详情(可选)">
          <a-form-item label="船舶隶属公司">
            <a-input
              v-model="newShipInfo.ownerCompany"
              placeholder="请输入船舶隶属公司"
            ></a-input>
          </a-form-item>
          <a-form-item label="船舶制造商">
            <a-input
              v-model="newShipInfo.manufacturer"
              placeholder="请输入船舶制造商"
            ></a-input>
          </a-form-item>

          <a-form-item label="MMSI号">
            <a-input
              v-model="newShipInfo.mmsi"
              placeholder="请输入MMSI号"
            ></a-input>
          </a-form-item>
          <a-form-item label="IMO号">
            <a-input
              v-model="newShipInfo.imoNumber"
              placeholder="请输入IMO号"
            ></a-input>
          </a-form-item>
          <a-form-item label="船舶总长">
            <a-input-number
              v-model="newShipInfo.length"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入船舶总长"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="垂线间长">
            <a-input-number
              v-model="newShipInfo.lbpLength"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入垂线间长"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="设计水线长">
            <a-input-number
              v-model="newShipInfo.designLwl"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入设计水线长"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="型宽">
            <a-input-number
              v-model="newShipInfo.moldedWidth"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入型宽"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="船舶宽度">
            <a-input-number
              v-model="newShipInfo.width"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入船舶宽度"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="型深">
            <a-input-number
              v-model="newShipInfo.moldedDepth"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入型深"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="设计吃水">
            <a-input-number
              v-model="newShipInfo.designDraft"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入设计吃水"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="最大吃水">
            <a-input-number
              v-model="newShipInfo.maxDraft"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入最大吃水"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="空载押水量">
            <a-input-number
              v-model="newShipInfo.noLoadDraft"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入空载押水量"
            ></a-input-number>
          </a-form-item>
          <a-form-item label="满载押水量">
            <a-input-number
              v-model="newShipInfo.maxLoadDraft"
              :min="0"
              :max="10000"
              :step="0.1"
              placeholder="请输入满载押水量"
            ></a-input-number>
          </a-form-item>
        </a-collapse-item>
      </a-collapse>

      <a-form-item>
        <a-button
          :loading="loading"
          type="primary"
          html-type="submit"
          style="width: 600px"
          >创建</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
  import { useStorage } from '@vueuse/core';
  import { ref } from 'vue';
  import { createNewShip, type ShipInfo } from '@/api/flow';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { useUserStore } from '@/store';

  const router = useRouter();
  const loading = ref(false);
  const newShipInfo = useStorage('newShipInfo', {
    name: '',
    type: '',
    model: '',
    ownerCompany: '',
    manufacturer: '',
    shipOriginNumber: '',
    mmsi: '',
    imoNumber: '',
    length: 0,
    lbpLength: 0,
    designLwl: 0,
    moldedWidth: 0,
    width: 0,
    moldedDepth: 0,
    designDraft: 0,
    maxDraft: 0,
    noLoadDraft: 0,
    maxLoadDraft: 0,
    progress: '',
    towingForce: '',
    maxBatteryCapacity: '',
    sailedDistance: 0,
  } as ShipInfo);
  const userStore = useUserStore();
  const handleSubmit = ({ errors, values }) => {
    if (!errors) {
      loading.value = true;
      createNewShip(values)
        .then(() => {
          loading.value = false;
          localStorage.removeItem('newShipInfo');
          userStore.info().finally(() => {
            router.push({ name: 'Workplace' });
          });
        })
        .catch((response) => {
          loading.value = false;
          Message.error({
            content: response.msg || '创建船舶信息失败',
            duration: 5 * 1000,
          });
        });
    }
  };
</script>

<style scoped>
  .extra-info {
    margin: 0 auto;
    margin-bottom: 15px;
    width: 850px;
  }
</style>
