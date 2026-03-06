<template>
  <div id="map-container" :style="mapContainerStyle"></div>
</template>

<script lang="ts" setup>
  import useThemes from '@/hooks/themes';
  // import { wgs84ToGcj02 } from '@/utils/gps';
  import { Position } from '@/store/modules/ship-data/type';
  import shipIconUrl from '@/assets/images/gaosulunchuan.png?url';
  import AMapLoader from '@amap/amap-jsapi-loader';
  import { onMounted, onUnmounted, PropType, ref, watch } from 'vue';

  const { isDark } = useThemes();
  const map = ref(null);
  const polyline = ref(null);

  const AMap = ref(null);
  const shipMarker = ref(null);

  const props = defineProps({
    shipLocation: {
      type: Object as PropType<Position>,
      default: () =>
        ({
          longitude: 119.420108,
          latitude: 34.752206,
        } as Position),
    },
    showMarker: {
      type: Boolean,
      default: true,
    },
    showTrace: {
      type: Boolean,
      default: true,
    },
    shipDirection: {
      type: Number,
      default: 0,
    },
    mapContainerStyle: {
      type: Object,
      default: () => ({
        width: '100%',
        height: '100%',
        padding: '0px',
        margin: '0px',
      }),
    },
    positionData: {
      type: Object as PropType<number[][]>,
    },
    center: {
      type: Object as PropType<number[]>,
      default: () => [119.420108, 34.752206] as number[],
    },
  });

  // const emits = defineEmits(['pathClick'])

  const drawLine = async (positions: number[][]) => {
    const path = [];
    if (map.value && AMap.value) {
      positions.forEach((position: number[]) => {
        // const gdPos = wgs84ToGcj02(position[0], position[1]);
        path.push(new AMap.value.LngLat(position[1], position[0]));
      });
      if (polyline.value) {
        polyline.value.setPath(path);
      } else {
        polyline.value = new AMap.value.Polyline({
          path,
          strokeWeight: 6, // 线条宽度
          strokeColor: 'red', // 线条颜色
          lineJoin: 'round', // 折线拐点连接处样式
          showDir: true, // 是否显示方向箭头
        });
        map.value.add(polyline.value);
        if (props.showTrace) {
          polyline.value.show();
        } else {
          polyline.value.hide();
        }
      }
    }
  };

  function initMap() {
    // window._AMapSecurityConfig = {
    //   securityJsCode: '7fa52993ea02e33132f819abe91b70a6',
    // };
    AMapLoader.load({
      key: '227342708918eaf839374fe2494ea945',
      version: '2.0',
      plugins: ['AMap.Scale'],
    }).then((_AMap) => {
      AMap.value = _AMap;
      map.value = new AMap.value.Map('map-container', {
        zoom: 12.5,
        center: props.center,
      });
      map.value.setMapStyle(
        isDark.value ? 'amap://styles/dark' : 'amap://styles/normal'
      );
      // map.value.on('click', (e) => {
      //   console.log(e.lnglat.getLng(), e.lnglat.getLat());
      // });
    });
  }

  const drawShipPosition = (posotion: Position, direction: number) => {
    if (map.value && AMap.value) {
      if (shipMarker.value) {
        shipMarker.value.setPosition(
          new AMap.value.LngLat(posotion.longitude, posotion.latitude)
        );
        shipMarker.value.setAngle(-90 + direction);
        // console.log('setAngle', -90 + direction);
      } else {
        shipMarker.value = new AMap.value.Marker({
          angle: -90 + direction,
          position: new AMap.value.LngLat(
            posotion.longitude,
            posotion.latitude
          ),
          icon: new AMap.value.Icon({
            image: shipIconUrl,
            size: new AMap.value.Size(24, 24),
            imageSize: new AMap.value.Size(24, 24),
          }),
          anchor: 'center',
        });
        map.value.add(shipMarker.value);
      }
      if (props.showMarker) {
        shipMarker.value.show();
      } else {
        shipMarker.value.hide();
      }
    } else {
      requestAnimationFrame(() => {
        drawShipPosition(posotion, direction);
      });
    }
  };

  onMounted(() => {
    initMap();
    drawLine(props.positionData);
    drawShipPosition(props.shipLocation, props.shipDirection);
    watch(
      () => props.positionData,
      (newVal) => {
        drawLine(newVal);
      }
    );
  });
  onUnmounted(() => {
    map.value?.destroy();
  });

  watch(
    () => isDark.value,
    (newVal) => {
      if (map.value) {
        map.value.setMapStyle(
          newVal ? 'amap://styles/dark' : 'amap://styles/normal'
        );
      }
    }
  );

  watch(
    () => props.showMarker,
    (newVal) => {
      if (shipMarker.value) {
        if (newVal) {
          shipMarker.value.show();
        } else {
          shipMarker.value.hide();
        }
      }
    }
  );

  watch(
    () => props.showTrace,
    (newVal) => {
      if (polyline.value) {
        if (newVal) {
          polyline.value.show();
        } else {
          polyline.value.hide();
        }
      }
    }
  );

  watch(
    () => props.shipLocation,
    (newVal) => {
      drawShipPosition(newVal, props.shipDirection);
    },
    {
      deep: true,
    }
  );

  watch(
    () => props.shipDirection,
    (newVal) => {
      drawShipPosition(props.shipLocation, newVal);
    }
  );
</script>
