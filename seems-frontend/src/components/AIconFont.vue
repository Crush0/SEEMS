<template>
  <icon-font v-bind="$attrs" />
</template>

<script>
  import { Icon } from '@arco-design/web-vue';
  // eslint-disable-next-line import/extensions
  import iconFontJS from '@/assets/js/iconfont.js?url';

  const IconFont = Icon.addFromIconFontCn({
    src: iconFontJS,
  });

  export default {
    components: {
      IconFont,
    },
  };
</script>
