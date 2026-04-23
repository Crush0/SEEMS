<template>
  <div class="modern-message-center">
    <a-card class="message-card" :bordered="false">
      <template #title>
        <div class="card-title-wrapper">
          <div class="title-icon">
            <icon-message :size="20" />
          </div>
          <span class="title-text">{{ $t('message.title') }}</span>
        </div>
      </template>

      <template #extra>
        <a-space :size="12">
          <a-button
            type="primary"
            @click="handleSendMessage"
          >
            <template #icon>
              <icon-send />
            </template>
            {{ $t('message.send') }}
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon>
              <icon-refresh />
            </template>
            {{ $t('message.refresh') }}
          </a-button>
        </a-space>
      </template>

      <a-row :gutter="20">
        <a-col :span="12">
          <div class="message-list-section">
            <a-tabs default-active-key="all" @change="handleTabChange">
              <a-tab-pane key="all">
                <template #title>
                  <span>{{ $t('message.tab.all') }}</span>
                  <a-badge :count="totalUnread" :max-count="99" />
                </template>

                <div class="messages-container">
                  <a-spin :loading="loading" style="width: 100%">
                    <div
                      v-for="message in allMessages"
                      :key="message.id"
                      class="message-item"
                      :class="{
                        'is-unread': message.status === 0,
                        'is-selected': selectedMessage?.id === message.id
                      }"
                      @click="handleSelectMessage(message)"
                    >
                      <div class="message-item-header">
                        <div class="sender-info">
                          <a-avatar :size="36" :image-url="message.senderAvatar">
                            {{ message.senderUsername?.charAt(0) || 'U' }}
                          </a-avatar>
                          <div class="sender-details">
                            <span class="sender-name">{{ message.senderUsername || $t('message.unknownSender') }}</span>
                            <span class="message-time">{{ formatTime(message.createDate) }}</span>
                          </div>
                        </div>
                        <div class="message-status">
                          <div v-if="message.status === 0" class="unread-dot"></div>
                        </div>
                      </div>

                      <div class="message-item-content">
                        <h3 class="message-title">{{ message.title }}</h3>
                        <p class="message-preview">{{ getPreview(message.content) }}</p>
                      </div>

                      <div v-if="message.type" class="message-type-badge" :class="`type-${message.type}`">
                        {{ $t(`message.type.${message.type}`) }}
                      </div>
                    </div>

                    <a-empty v-if="!loading && allMessages.length === 0" :description="$t('message.noMessages')" />
                  </a-spin>
                </div>

                <div class="pagination-wrapper">
                  <a-pagination
                    v-model:current="pagination.current"
                    v-model:page-size="pagination.pageSize"
                    :total="pagination.total"
                    :show-total="true"
                    :show-jumper="true"
                    :show-page-size="true"
                    @change="handlePageChange"
                  />
                </div>
              </a-tab-pane>

              <a-tab-pane key="unread">
                <template #title>
                  <span>{{ $t('message.tab.unread') }}</span>
                  <a-badge :count="unreadCount" :max-count="99" />
                </template>

                <div class="messages-container">
                  <a-spin :loading="loading" style="width: 100%">
                    <div
                      v-for="message in unreadMessages"
                      :key="message.id"
                      class="message-item"
                      :class="{
                        'is-unread': message.status === 0,
                        'is-selected': selectedMessage?.id === message.id
                      }"
                      @click="handleSelectMessage(message)"
                    >
                      <div class="message-item-header">
                        <div class="sender-info">
                          <a-avatar :size="36" :image-url="message.senderAvatar">
                            {{ message.senderUsername?.charAt(0) || 'U' }}
                          </a-avatar>
                          <div class="sender-details">
                            <span class="sender-name">{{ message.senderUsername || $t('message.unknownSender') }}</span>
                            <span class="message-time">{{ formatTime(message.createDate) }}</span>
                          </div>
                        </div>
                        <div class="message-status">
                          <div v-if="message.status === 0" class="unread-dot"></div>
                        </div>
                      </div>

                      <div class="message-item-content">
                        <h3 class="message-title">{{ message.title }}</h3>
                        <p class="message-preview">{{ getPreview(message.content) }}</p>
                      </div>

                      <div v-if="message.type" class="message-type-badge" :class="`type-${message.type}`">
                        {{ $t(`message.type.${message.type}`) }}
                      </div>
                    </div>

                    <a-empty v-if="!loading && unreadMessages.length === 0" :description="$t('message.noMessages')" />
                  </a-spin>
                </div>

                <div class="pagination-wrapper">
                  <a-pagination
                    v-model:current="pagination.current"
                    v-model:page-size="pagination.pageSize"
                    :total="pagination.total"
                    :show-total="true"
                    :show-jumper="true"
                    :show-page-size="true"
                    @change="handlePageChange"
                  />
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-col>

        <a-col :span="12">
          <div class="message-detail-section">
            <a-spin :loading="detailLoading" style="width: 100%; height: 100%">
              <div v-if="!selectedMessage" class="detail-empty">
                <a-empty :description="$t('message.noSelection')" />
              </div>

              <div v-else class="message-detail">
                <div class="detail-header">
                  <div class="detail-sender">
                    <a-avatar :size="48" :image-url="selectedMessage.senderAvatar">
                      {{ selectedMessage.senderUsername?.charAt(0) || 'U' }}
                    </a-avatar>
                    <div class="sender-info">
                      <div class="sender-name">{{ selectedMessage.senderUsername || $t('message.unknownSender') }}</div>
                      <div class="send-time">{{ formatTime(selectedMessage.createDate) }}</div>
                    </div>
                  </div>
                  <div class="detail-status">
                    <a-tag :color="selectedMessage.status === 0 ? 'red' : 'green'">
                      {{ selectedMessage.status === 0 ? $t('message.status.unread') : $t('message.status.read') }}
                    </a-tag>
                  </div>
                </div>

                <a-divider />

                <div class="detail-content">
                  <h2 class="message-title">{{ selectedMessage.title }}</h2>
                  <div class="message-body">{{ selectedMessage.content }}</div>
                </div>

                <a-divider />

                <div class="detail-footer">
                  <a-space>
                    <a-button
                      v-if="selectedMessage.status === 0"
                      type="primary"
                      @click="handleMarkAsRead"
                    >
                      <template #icon>
                        <icon-check />
                      </template>
                      {{ $t('message.markAsRead') }}
                    </a-button>
                    <a-tag v-if="selectedMessage.type" color="blue">
                      {{ $t(`message.type.${selectedMessage.type}`) }}
                    </a-tag>
                    <a-tag v-if="selectedMessage.isRoleMessage" color="purple">
                      {{ $t('message.roleMessage') }}: {{ $t(`message.role.${selectedMessage.role}`) }}
                    </a-tag>
                  </a-space>
                </div>
              </div>
            </a-spin>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <SendMessageModal
      v-model:visible="sendModalVisible"
      @success="handleSendSuccess"
    />
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { IconMessage, IconSend, IconRefresh, IconCheck } from '@arco-design/web-vue/es/icon';
  import {
    MessageRecord,
    queryMessageList,
    setMessageStatus,
    getUnreadCount
  } from '@/api/message';
  import SendMessageModal from './components/send-message-modal.vue';
  import useLoading from '@/hooks/loading';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const detailLoading = ref(false);

  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const allMessages = ref<MessageRecord[]>([]);
  const unreadMessages = ref<MessageRecord[]>([]);
  const activeTab = ref<'all' | 'unread'>('all');

  const unreadCount = ref(0);
  const totalUnread = computed(() => unreadCount.value);

  const selectedMessage = ref<MessageRecord | null>(null);
  const sendModalVisible = ref(false);

  const fetchMessageList = async (status?: 0 | 1) => {
    setLoading(true);
    try {
      const params: any = {
        current: pagination.value.current,
        pageSize: pagination.value.pageSize,
      };

      if (status !== undefined) {
        params.status = status;
      }

      const { data } = await queryMessageList(params);

      if (status === undefined) {
        allMessages.value = data.list;
        pagination.value.total = data.total;
      } else if (status === 0) {
        unreadMessages.value = data.list;
        pagination.value.total = data.total;
      }
    } catch (error) {
      console.error('获取消息列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      unreadCount.value = await getUnreadCount();
    } catch (error) {
      console.error('获取未读数量失败:', error);
    }
  };

  const handleTabChange = (key: string) => {
    activeTab.value = key as 'all' | 'unread';
    pagination.value.current = 1;

    if (key === 'unread') {
      fetchMessageList(0);
    } else {
      fetchMessageList();
    }
  };

  const handleSelectMessage = (message: MessageRecord) => {
    selectedMessage.value = message;
  };

  const handleMarkAsRead = async () => {
    if (!selectedMessage.value) return;
    try {
      await setMessageStatus([selectedMessage.value.id]);
      selectedMessage.value.status = 1;
      fetchMessageList();
      fetchUnreadCount();
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  };

  const handleSendMessage = () => {
    sendModalVisible.value = true;
  };

  const handleSendSuccess = () => {
    sendModalVisible.value = false;
    fetchMessageList();
  };

  const handleRefresh = () => {
    fetchMessageList();
    fetchUnreadCount();
  };

  const handlePageChange = () => {
    if (activeTab.value === 'unread') {
      fetchMessageList(0);
    } else {
      fetchMessageList();
    }
  };

  const getPreview = (content: string, maxLength = 80) => {
    if (!content) return '';
    return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
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

  onMounted(() => {
    fetchMessageList();
    fetchUnreadCount();
  });
</script>

<style scoped lang="less">
  .modern-message-center {
    padding: 20px;
  }

  .card-title-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;

    .title-icon {
      color: rgb(var(--primary-6));
    }

    .title-text {
      color: var(--color-text-1);
    }
  }

  .message-list-section {
    height: calc(100vh - 280px);
    display: flex;
    flex-direction: column;

    :deep(.arco-tabs) {
      display: flex;
      flex-direction: column;
      height: 100%;

      .arco-tabs-nav {
        flex-shrink: 0;
      }

      .arco-tabs-content {
        flex: 1;
        overflow: hidden;
      }

      .arco-tabs-content-list {
        height: 100%;
      }

      .arco-tabs-content-item,
      .arco-tabs-content-item-active {
        height: 100%;
        display: flex;
        flex-direction: column;
      }

      .arco-tabs-pane {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }
    }
  }

  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding-right: 4px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: var(--color-fill-2);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--color-fill-3);
      border-radius: 3px;

      &:hover {
        background: var(--color-fill-4);
      }
    }
  }

  .message-item {
    background: var(--color-bg-2);
    border: 1px solid var(--color-border-2);
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;
    overflow: hidden;

    &:hover {
      border-color: rgb(var(--primary-6));
      box-shadow: 0 2px 8px rgba(var(--primary-6), 0.1);
      transform: translateX(2px);
    }

    &.is-unread {
      background: var(--color-fill-1);
      border-left: 3px solid rgb(var(--primary-6));

      .message-title {
        font-weight: 600;
        color: rgb(var(--primary-6));
      }
    }

    &.is-selected {
      border-color: rgb(var(--primary-6));
      background: var(--color-fill-3);
      box-shadow: 0 2px 12px rgba(var(--primary-6), 0.15);
    }
  }

  .message-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .sender-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .sender-details {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .sender-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-1);
    }

    .message-time {
      font-size: 12px;
      color: var(--color-text-3);
    }
  }

  .message-status {
    .unread-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: rgb(var(--danger-6));
      box-shadow: 0 0 6px rgba(var(--danger-6), 0.4);
    }
  }

  .message-item-content {
    margin-bottom: 12px;

    .message-title {
      font-size: 15px;
      font-weight: 500;
      color: var(--color-text-1);
      margin: 0 0 8px 0;
    }

    .message-preview {
      font-size: 13px;
      color: var(--color-text-2);
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .message-type-badge {
    position: absolute;
    top: 16px;
    right: 16px;
    font-size: 11px;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: 4px;
    text-transform: uppercase;
    letter-spacing: 0.5px;

    &.type-message {
      background: rgba(var(--primary-6), 0.1);
      color: rgb(var(--primary-6));
    }

    &.type-notice {
      background: rgba(var(--warning-6), 0.1);
      color: rgb(var(--warning-6));
    }

    &.type-todo {
      background: rgba(var(--success-6), 0.1);
      color: rgb(var(--success-6));
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 16px 0 0 0;
    flex-shrink: 0;
    background: var(--color-bg-1);
  }

  .message-detail-section {
    height: calc(100vh - 280px);
    background: var(--color-bg-2);
    border: 1px solid var(--color-border-2);
    border-radius: 8px;
    padding: 20px;
    display: flex;
    flex-direction: column;
  }

  .detail-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }

  .message-detail {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .detail-sender {
    display: flex;
    align-items: center;
    gap: 12px;

    .sender-info {
      .sender-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--color-text-1);
        margin-bottom: 4px;
      }

      .send-time {
        font-size: 12px;
        color: var(--color-text-3);
      }
    }
  }

  .detail-content {
    flex: 1;
    overflow-y: auto;

    .message-title {
      font-size: 20px;
      font-weight: 600;
      color: var(--color-text-1);
      margin-bottom: 16px;
    }

    .message-body {
      font-size: 14px;
      color: var(--color-text-2);
      line-height: 1.8;
      white-space: pre-wrap;
    }
  }

  .detail-footer {
    margin-top: 16px;
  }
</style>
