<template>
  <div class="message-list">
    <a-spin :loading="loading" style="width: 100%">
      <a-list :data="messages" :bordered="false">
        <template #item="{ item }">
          <a-list-item
            class="message-item"
            :class="{ 'is-unread': item.status === 0, 'is-selected': selectedId === item.id }"
            @click="handleClick(item)"
          >
            <template #extra>
              <a-tag v-if="item.status === 0" color="red" size="small">
                {{ $t('message.status.unread') }}
              </a-tag>
            </template>
            <a-list-item-meta>
              <template #avatar>
                <a-avatar :size="40" :image-url="item.senderAvatar">
                  {{ item.senderUsername?.charAt(0) || 'U' }}
                </a-avatar>
              </template>
              <template #title>
                <div class="item-title">
                  <span class="title-text">{{ item.title }}</span>
                  <a-tag v-if="item.type" size="small" color="arcoblue">
                    {{ $t(`message.type.${item.type}`) }}
                  </a-tag>
                </div>
              </template>
              <template #description>
                <div class="item-description">
                  <div class="sender-name">{{ item.senderUsername || $t('message.unknownSender') }}</div>
                  <div class="send-time">{{ formatTime(item.createDate) }}</div>
                </div>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
        <template #empty>
          <a-empty :description="$t('message.noMessages')" />
        </template>
      </a-list>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { MessageRecord } from '@/api/message';

  interface Props {
    messages: MessageRecord[];
    loading?: boolean;
  }

  interface Emits {
    (e: 'select', message: MessageRecord): void;
  }

  const props = withDefaults(defineProps<Props>(), {
    loading: false,
  });

  const emit = defineEmits<Emits>();
  const { t } = useI18n();

  const selectedId = ref<string>('');

  const handleClick = (message: MessageRecord) => {
    selectedId.value = message.id;
    emit('select', message);
  };

  const formatTime = (dateStr?: string) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (seconds < 60) return t('message.time.justNow');
    if (minutes < 60) return t('message.time.minutesAgo', { n: minutes });
    if (hours < 24) return t('message.time.hoursAgo', { n: hours });
    if (days < 7) return t('message.time.daysAgo', { n: days });

    return date.toLocaleString();
  };
</script>

<style scoped lang="less">
  .message-list {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    :deep(.arco-list) {
      flex: 1;
      overflow-y: auto;

      .arco-list-item {
        border: none;
      }

      .arco-list-item-meta {
        align-items: flex-start;
      }
    }
  }

  .message-item {
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s;
    width: 95%;
    margin: 8px;
    &:hover {
      background: var(--color-fill-2);
    }

    &.is-unread {
      background: var(--color-primary-light-1);
      border-left: 3px solid rgb(var(--primary-6));

      .item-title .title-text {
        font-weight: 600;
      }
    }

    &.is-selected {
      background: var(--color-primary-light-3);
      border-left: 3px solid rgb(var(--primary-6));
    }

    .item-title {
      display: flex;
      align-items: center;
      gap: 8px;

      .title-text {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .item-description {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 4px;

      .sender-name {
        font-size: 12px;
        color: var(--color-text-2);
      }

      .send-time {
        font-size: 12px;
        color: var(--color-text-3);
      }
    }
  }
</style>
