<template>
  <div class="header">
    <a-modal
      v-model:visible="showCropper"
      title="修改头像"
      width="fit-content"
      @before-ok="handleBeforeOk"
    >
      <VuePictureCropper
        :box-style="{
          width: '400px',
          height: '400px',
          backgroundColor: '#f8f8f8',
          margin: 'auto',
        }"
        :img="pic"
        :options="{
          viewMode: 1,
          dragMode: 'crop',
          aspectRatio: 1,
        }"
        :preset-mode="{
          mode: 'round',
        }"
        @ready="onReady"
      />
    </a-modal>
    <a-space :size="12" direction="vertical" align="center">
      <input
        ref="uploadInput"
        style="display: none"
        type="file"
        accept="image/jpg, image/jpeg, image/png, image/gif"
        @change="selectFile"
      />
      <a-avatar :size="64" @click="handleChangeAvatar">
        <template #trigger-icon>
          <icon-camera />
        </template>
        <img :src="userInfo.avatar" />
      </a-avatar>
      <a-typography-title :heading="6" style="margin: 0">
        {{ userInfo.username }}
      </a-typography-title>
      <div class="user-msg">
        <a-space :size="18">
          <div>
            <icon-user />
            <a-typography-text>{{
              $t(`userInfo.role.${userInfo.role}`)
            }}</a-typography-text>
          </div>
          <div>
            <AIconFont type="icon-ship" />
            <a-typography-text>
              {{ shipDataStore.getShipInfo.name }}
            </a-typography-text>
          </div>
        </a-space>
      </div>
    </a-space>
  </div>
</template>

<script lang="ts" setup>
  import VuePictureCropper, { cropper } from 'vue-picture-cropper';
  import { useShipDataStore, useUserStore } from '@/store';
  import { reactive, ref } from 'vue';
  import { updateAvatar } from '@/api/user';
  import { Message } from '@arco-design/web-vue';
  import AIconFont from '@/components/AIconFont.vue';

  const showCropper = ref(false);
  const pic = ref('');
  const uploadInput = ref<HTMLInputElement | null>(null);
  const result = reactive({
    dataURL: '',
    blobURL: '',
  });
  const onReady = () => {
    /* empty */
  };
  const shipDataStore = useShipDataStore();
  const userInfo = useUserStore();
  const handleChangeAvatar = () => {
    uploadInput.value?.click();
  };
  function selectFile(e: Event) {
    // Reset last selection and results
    pic.value = '';
    result.dataURL = '';
    result.blobURL = '';

    // Get selected files
    const { files } = e.target as HTMLInputElement;
    if (!files || !files.length) return;

    // Convert to dataURL and pass to the cropper component
    const file = files[0];
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      // Update the picture source of the `img` prop
      pic.value = String(reader.result);

      // Show the modal
      showCropper.value = true;

      // Clear selected files of input element
      if (!uploadInput.value) return;
      uploadInput.value.value = '';
    };
  }
  function reset() {
    if (!cropper) return;
    cropper.reset();
  }
  async function getResult() {
    if (!cropper) return;
    const base64 = cropper.getDataURL();
    const blob: Blob | null = await cropper.getBlob();
    if (!blob) return;

    result.dataURL = base64;
    result.blobURL = URL.createObjectURL(blob);
    showCropper.value = false;
    reset();
  }

  function dealImage(base64, w, callback) {
    const newImage = new Image();
    let quality = 0.6; // 压缩系数0-1之间
    newImage.src = base64;
    newImage.setAttribute('crossOrigin', 'Anonymous'); // url为外域时需要
    let imgWidth;
    let imgHeight;
    newImage.onload = () => {
      imgWidth = newImage.width;
      imgHeight = newImage.height;
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      if (Math.max(imgWidth, imgHeight) > w) {
        if (imgWidth > imgHeight) {
          canvas.width = w;
          canvas.height = (w * imgHeight) / imgWidth;
        } else {
          canvas.height = w;
          canvas.width = (w * imgWidth) / imgHeight;
        }
      } else {
        canvas.width = imgWidth;
        canvas.height = imgHeight;
        quality = 0.6;
      }
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(newImage, 0, 0, canvas.width, canvas.height);
      const base64Data = canvas.toDataURL('image/jpeg', quality); // 压缩语句
      // 如想确保图片压缩到自己想要的尺寸,如要求在50-150kb之间，请加以下语句，quality初始值根据情况自定
      // while (base64.length / 1024 > 150) {
      // 	quality -= 0.01;
      // 	base64 = canvas.toDataURL("image/jpeg", quality);
      // }
      // 防止最后一次压缩低于最低尺寸，只要quality递减合理，无需考虑
      // while (base64.length / 1024 < 50) {
      // 	quality += 0.001;
      // 	base64 = canvas.toDataURL("image/jpeg", quality);
      // }
      callback(base64Data); // 必须通过回调函数返回，否则无法及时拿到该值
    };
  }
  const handleBeforeOk = (done) => {
    getResult().then(() => {
      dealImage(result.dataURL, 200, (base64) => {
        updateAvatar(base64)
          .then(() => {
            done();
            window.location.reload();
          })
          .catch(() => {
            Message.error('上传失败，请稍后再试');
            done(false);
          });
      });
    });
  };
</script>

<style scoped lang="less">
  .header {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 204px;
    color: var(--gray-10);
    background: url(//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/41c6b125cc2e27021bf7fcc9a9b1897c.svg~tplv-49unhts6dw-image.image)
      no-repeat;
    background-size: cover;
    border-radius: 4px;

    :deep(.arco-avatar-trigger-icon-button) {
      color: rgb(var(--arcoblue-6));

      :deep(.arco-icon) {
        vertical-align: -1px;
      }
    }
    .user-msg {
      .arco-icon {
        color: rgb(var(--gray-10));
      }
      .arco-typography {
        margin-left: 6px;
      }
    }
  }
</style>
