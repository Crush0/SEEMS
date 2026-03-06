<template>
  <a-modal
    v-model:visible="visible"
    class="edit-report-modal"
    :on-before-ok="handleBeforeClose"
    :mask-closable="false"
  >
    <template #title> 编辑报告 </template>
    <a-form ref="form" :model="model" :rules="rules" auto-label-width>
      <a-form-item label="理论航速(knot)" field="theorySpeed">
        <a-input-number
          v-model="model.theorySpeed"
          @input="handleTheorySpeedInput"
        />
      </a-form-item>
      <a-form-item label="实际航速(knot)" field="actualSpeed">
        <a-input-number
          v-model="model.actualSpeed"
          @input="handleActualSpeedInput"
        />
      </a-form-item>
      <a-form-item label="航向角(°)" field="direction">
        <a-input-number v-model="model.direction" />
      </a-form-item>
      <a-form-item label="风速(米每秒)" field="windSpeed">
        <a-input-number v-model="model.windSpeed" />
      </a-form-item>
      <a-form-item label="风向(°)" field="windDirection">
        <a-input-number v-model="model.windDirection" />
      </a-form-item>
      <a-form-item label="经度" field="longitude">
        <a-input-number v-model="model.longitude" />
      </a-form-item>
      <a-form-item label="纬度" field="latitude">
        <a-input-number v-model="model.latitude" />
      </a-form-item>
      <a-form-item label="实际航程(km)" field="actualVoyage">
        <a-input-number v-model="model.actualVoyage" :precision="2" />
      </a-form-item>
      <a-form-item label="累计航程(km)" field="totalVoyage">
        <a-input-number v-model="model.totalVoyage" :precision="2" />
      </a-form-item>
      <a-form-item label="滑失率(%)" field="slidingRate">
        <a-input-number v-model="model.slidingRate" :precision="2" />
      </a-form-item>
      <a-form-item label="生成时间" field="time">
        <a-date-picker v-model="model.time" show-time />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { PropType, ref } from 'vue';
  import { Report, updateOrInsertReport } from '@/api/report';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { Message } from '@arco-design/web-vue';

  const emits = defineEmits(['onOk']);
  const form = ref<FormInstance | null>(null);
  const visible = defineModel('visible', {
    type: Boolean,
    required: true,
  });
  const model = defineModel('model', {
    type: Object as PropType<Report>,
    required: true,
  });
  const rules = {
    theorySpeed: [
      { required: true, message: '理论航速不能为空', trigger: 'blur' },
      { type: 'number', message: '理论航速必须为数字', trigger: 'blur' },
    ],
    actualSpeed: [
      { required: true, message: '实际航速不能为空', trigger: 'blur' },
      { type: 'number', message: '实际航速必须为数字', trigger: 'blur' },
    ],
    direction: [
      { required: true, message: '航向角不能为空', trigger: 'blur' },
      { type: 'number', message: '航向角必须为数字', trigger: 'blur' },
    ],
    windSpeed: [
      { required: true, message: '风速不能为空', trigger: 'blur' },
      { type: 'number', message: '风速必须为数字', trigger: 'blur' },
    ],
    windDirection: [
      { required: true, message: '风向不能为空', trigger: 'blur' },
      { type: 'number', message: '风向必须为数字', trigger: 'blur' },
    ],
    longitude: [
      { required: true, message: '经度不能为空', trigger: 'blur' },
      { type: 'number', message: '经度必须为数字', trigger: 'blur' },
    ],
    latitude: [
      { required: true, message: '纬度不能为空', trigger: 'blur' },
      { type: 'number', message: '纬度必须为数字', trigger: 'blur' },
    ],
    actualVoyage: [
      { required: true, message: '实际航程不能为空', trigger: 'blur' },
      { type: 'number', message: '实际航程必须为数字', trigger: 'blur' },
    ],
    totalVoyage: [
      { required: true, message: '累计航程不能为空', trigger: 'blur' },
      { type: 'number', message: '累计航程必须为数字', trigger: 'blur' },
    ],
    slidingRate: [
      { required: true, message: '滑失率不能为空', trigger: 'blur' },
      { type: 'number', message: '滑失率必须为数字', trigger: 'blur' },
    ],
    time: [{ required: true, message: '生成时间不能为空', trigger: 'blur' }],
  };

  const handleTheorySpeedInput = (value: number) => {
    // console.log('theorySpeed', value);
    try {
      model.value.slidingRate =
        ((value - model.value.actualSpeed) / value) * 100;
    } catch (ignored) {
      /* empty */
    }
  };

  const handleActualSpeedInput = (value: number) => {
    // console.log('actualSpeed', value);
    try {
      model.value.slidingRate =
        ((model.value.theorySpeed - value) / model.value.theorySpeed) * 100;
    } catch (ignored) {
      /* empty */
    }
  };

  const handleBeforeClose = (done: (bool?: boolean) => void) => {
    if (form.value) {
      form.value.validate((errors) => {
        if (errors) {
          done(false);
        } else {
          updateOrInsertReport(model.value).then(() => {
            Message.success('修改成功');
            emits('onOk');
            done(true);
          });
        }
      });
    }
  };
</script>

<style scoped></style>
