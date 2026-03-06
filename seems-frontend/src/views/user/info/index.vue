<template>
  <div class="container">
    <a-modal
      v-model:visible="visible"
      :width="800"
      title="编辑船舶信息"
      @before-ok="handleSubmit"
    >
      <a-form ref="formRef" :model="newShipInfo" auto-label-width>
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
      </a-form>
    </a-modal>
    <Breadcrumb :items="['menu.user', 'menu.user.info']" />
    <a-row style="margin-bottom: 16px">
      <a-col :span="24">
        <UserInfoHeader />
      </a-col>
    </a-row>
    <a-row class="wrapper">
      <a-col :offset="4" :span="8">
        <a-tabs default-active-key="1" type="rounded">
          <a-tab-pane key="1" title="用户信息">
            <BasicInformation />
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :span="8">
        <a-tabs default-active-key="1" type="rounded">
          <template #extra>
            <a-button
              v-if="userStore.role === 'admin'"
              v-permission="['admin']"
              type="primary"
              @click="handleShipInfoEdit"
              >编辑</a-button
            >
          </template>
          <a-tab-pane key="1" title="船只信息">
            <ShipInformation />
          </a-tab-pane>
        </a-tabs>
      </a-col>
    </a-row>

    <!-- <div class="content">
      <div class="content-left">
        <a-grid :cols="24" :col-gap="16" :row-gap="16">
          <a-grid-item :span="24">
            <MyProject />
          </a-grid-item>
          <a-grid-item :span="24">
            <LatestActivity />
          </a-grid-item>
        </a-grid>
      </div>
      <div class="content-right">
        <a-grid :cols="24" :row-gap="16">
          <a-grid-item :span="24">
            <MyTeam />
          </a-grid-item>
          <a-grid-item class="panel" :span="24">
            <LatestNotification />
          </a-grid-item>
        </a-grid>
      </div>
    </div> -->
  </div>
</template>

<script lang="ts" setup>
  import { useShipDataStore, useUserStore } from '@/store';
  import { ref } from 'vue';
  import { ShipInfo, updateShipInfo } from '@/api/flow';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import ShipInformation from './components/ship-information.vue';
  import BasicInformation from './components/basic-information.vue';
  import UserInfoHeader from './components/user-info-header.vue';

  const userStore = useUserStore();
  const shipDataStore = useShipDataStore();
  const visible = ref(false);
  const formRef = ref<FormInstance | null>(null);
  const newShipInfo = ref({} as ShipInfo);
  const handleShipInfoEdit = () => {
    newShipInfo.value = {
      ...shipDataStore.getShipInfo,
    };
    visible.value = true;
  };
  const handleSubmit = (done) => {
    formRef.value?.validate((errors) => {
      if (errors) {
        done(false);
        return;
      }
      updateShipInfo(newShipInfo.value)
        .then(() => {
          shipDataStore.updateShipInfo({ shipInfo: newShipInfo.value });
          Message.success('更新船舶信息成功');
          done();
        })
        .catch(() => {
          Message.error('更新船舶信息失败');
          done(false);
        });
    });
  };
</script>

<script lang="ts">
  export default {
    name: 'Info',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }

  .content {
    display: flex;
    margin-top: 12px;

    &-left {
      flex: 1;
      margin-right: 16px;
      overflow: hidden;
      // background-color: var(--color-bg-2);

      :deep(.arco-tabs-nav-tab) {
        margin-left: 16px;
      }
    }

    &-right {
      width: 332px;
    }

    .tab-pane-wrapper {
      padding: 0 16px 16px 16px;
    }
  }
  .wrapper {
    padding: 20px 0 0 20px;
    min-height: 580px;
    background-color: var(--color-bg-2);
    border-radius: 4px;
  }
</style>

<style lang="less" scoped>
  .mobile {
    .content {
      display: block;
      &-left {
        margin-right: 0;
        margin-bottom: 16px;
      }
      &-right {
        width: 100%;
      }
    }
  }
</style>
