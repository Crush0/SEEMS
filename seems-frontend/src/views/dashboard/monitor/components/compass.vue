<template>
  <TresCanvas v-bind="gl">
    <TresPerspectiveCamera
      ref="camera"
      :position="position"
      :fov="45"
      :aspect="1"
      :near="0.1"
      :far="1000"
    />
    <OrbitControls />
    <Suspense>
      <Tugboat ref="tug" :rotate="outputRotate" />
    </Suspense>
    <Suspense>
      <TresMesh
        receive-shadow
        :position="[0, -2, 0]"
        :rotation="[Math.PI * 1.5, 0, Math.PI / 2]"
        :scale="[15, 15, 15]"
      >
        <TresPlaneGeometry :width="418" :height="418" />
        <TresMeshBasicMaterial
          :map="isDark ? darkTexture.map : lightTexture.map"
          :side="2"
          :transparent="true"
          :depth-test="true"
        />
      </TresMesh>
    </Suspense>

    <TresDirectionalLight :position="[0, 2, 4]" :intensity="1.2" cast-shadow />
    <TresAmbientLight :intensity="2" />
  </TresCanvas>
</template>

<script lang="ts" setup>
  import { TresCanvas, useTexture } from '@tresjs/core';
  import { OrbitControls } from '@tresjs/cientos';
  import { computed, ref, shallowRef } from 'vue';
  import { TransitionPresets, useTransition } from '@vueuse/core';
  import compassPng from '@/assets/images/compass-light.png?url';
  import darkModeCompassPng from '@/assets/images/compass-dark.png?url';
  import { useShipDataStore } from '@/store';
  import useThemes from '@/hooks/themes';
  import Tugboat from './tugboat.vue';

  const tug = shallowRef();
  const position = ref([5, 20, -1.5]);
  const shipDataStore = useShipDataStore();
  const realTimeData = computed(() => shipDataStore.getRealTimeData);
  const rotate = computed(() => realTimeData.value.direction ?? 0);
  const outputRotate = useTransition(rotate, {
    duration: 1000,
    transition: TransitionPresets.easeInOutCubic,
  });
  const { isDark } = useThemes();
  const lightTexture = await useTexture({
    map: compassPng,
  });

  const darkTexture = await useTexture({
    map: darkModeCompassPng,
  });

  const gl = {
    // clearColor: '#F78B3D',
    shadows: false,
  };
</script>

<style scoped></style>
