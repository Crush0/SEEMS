<template>
  <primitive :object="scene" />
</template>

<script setup lang="ts">
  import { useGLTF } from '@tresjs/cientos';
  import boatModel from '@/assets/model/tugboat/tugboat_v1.gltf?url';
  import { watch } from 'vue';

  const { scene } = await useGLTF(
    boatModel,
    {
      draco: true,
    }
    // (loader) => {
    //   loader.setDRACOLoader(new DRACOLoader());
    // }
  );
  const props = defineProps({
    rotate: {
      type: Number,
      default: 0,
    },
  });
  scene.position.set(0, 0, 0);

  scene.rotation.set(Math.PI * (2 / 4), Math.PI, Math.PI);
  watch(
    () => props.rotate,
    (newVal) => {
      scene.rotation.z = Math.PI + ((Math.PI * newVal) / 180) * -1;
    },
    {
      immediate: true,
    }
  );
  scene.scale.set(0.0018, 0.0018, 0.0018);
  scene.traverse((child) => {
    child.frustumCulled = false;
  });
</script>

<style scoped></style>
