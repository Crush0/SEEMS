<template>
  <div class="message-container">
    <a-card class="message-card" :bordered="false">
      <template #title>
        <div class="card-title">
          <icon-message />
          {{ $t('message.title') }}
        </div>
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleSendMessage">
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
                <MessageList
                  :messages="currentMessages"
                  :loading="loading"
                  @select="handleSelectMessage"
                />
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
                <MessageList
                  :messages="currentMessages"
                  :loading="loading"
                  @select="handleSelectMessage"
                />
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
              <div v-if="selectedMessage" class="message-detail">
                <div class="detail-header">
                  <div class="sender-info">
                    <a-avatar
                      :size="48"
                      :image-url="selectedMessage.senderAvatar || defaultAvatar"
                    >
                      {{ selectedMessage.senderUsername?.charAt(0) || 'U' }}
                    </a-avatar>
                    <div class="sender-text">
                      <div class="sender-name">{{ selectedMessage.senderUsername }}</div>
                      <div class="send-time">{{ formatTime(selectedMessage.createDate) }}</div>
                    </div>
                  </div>
                  <div class="message-status">
                    <a-tag
                      :color="selectedMessage.status === 0 ? 'red' : 'green'"
                    >
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
              <a-empty v-else :description="$t('message.noSelection')" />
            </a-spin>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 发送消息弹窗 -->
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
  import { MessageRecord, queryMessageList, setMessageStatus, getUnreadCount } from '@/api/message';
  import MessageList from './components/message-list.vue';
  import SendMessageModal from './components/send-message-modal.vue';
  import useLoading from '@/hooks/loading';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const detailLoading = ref(false);

  const defaultAvatar = 'https://via.placeholder.com/48';

  // 分页
  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  // 消息数据
  const allMessages = ref<MessageRecord[]>([]);
  const unreadMessages = ref<MessageRecord[]>([]);
  const activeTab = ref<'all' | 'unread'>('all');

  // 未读数量
  const unreadCount = ref(0);
  const totalUnread = computed(() => unreadCount.value);

  // 当前显示的消息列表
  const currentMessages = computed(() => {
    return activeTab.value === 'all' ? allMessages.value : unreadMessages.value;
  });

  // 选中的消息
  const selectedMessage = ref<MessageRecord | null>(null);

  // 发送消息弹窗
  const sendModalVisible = ref(false);

  // 获取消息列表
  const fetchMessageList = async (status?: 0 | 1) => {
    setLoading(true);
    try {
      // 构建请求参数，只在 status 有值时才传递
      const params: any = {
        current: pagination.value.current,
        pageSize: pagination.value.pageSize,
      };

      if (status !== undefined) {
        params.status = status;
      }

      const { data } = await queryMessageList(params);

      // 根据查询类型更新对应的数据
      if (status === undefined) {
        // 查询全部消息
        allMessages.value = data.list;
        pagination.value.total = data.total;
      } else if (status === 0) {
        // 查询未读消息
        unreadMessages.value = data.list;
        pagination.value.total = data.total;
      }
    } catch (error) {
      // 错误处理
      console.error('获取消息列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 获取未读数量
  const fetchUnreadCount = async () => {
    try {
      unreadCount.value = await getUnreadCount();
    } catch (error) {
      // 错误处理
    }
  };

  // 标签页切换
  const handleTabChange = (key: string) => {
    activeTab.value = key as 'all' | 'unread';
    pagination.value.current = 1; // 重置页码

    if (key === 'unread') {
      // 查询未读消息
      fetchMessageList(0);
    } else {
      // 查询全部消息
      fetchMessageList();
    }
  };

  // 分页变化
  const handlePageChange = () => {
    if (activeTab.value === 'unread') {
      fetchMessageList(0);
    } else {
      fetchMessageList();
    }
  };

  // 选择消息
  const handleSelectMessage = (message: MessageRecord) => {
    selectedMessage.value = message;
  };

  // 标记为已读
  const handleMarkAsRead = async () => {
    if (!selectedMessage.value) return;
    try {
      await setMessageStatus([selectedMessage.value.id]);
      // 更新本地状态
      selectedMessage.value.status = 1;
      // 刷新列表和未读数量
      fetchMessageList();
      fetchUnreadCount();
    } catch (error) {
      // 错误处理
    }
  };

  // 发送消息
  const handleSendMessage = () => {
    sendModalVisible.value = true;
  };

  // 发送成功回调
  const handleSendSuccess = () => {
    sendModalVisible.value = false;
    fetchMessageList();
  };

  // 刷新
  const handleRefresh = () => {
    fetchMessageList();
    fetchUnreadCount();
  };

  // 格式化时间
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
  .message-container {
    padding: 20px;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
  }

  .message-list-section {
    height: calc(100vh - 280px);
    display: flex;
    flex-direction: column;
  }

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

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    flex-shrink: 0;
    padding: 12px 0 0 0;
    margin: 0;
    background: var(--color-bg-2);
  }

  .message-detail-section {
    height: calc(100vh - 280px);
    background: var(--color-fill-2);
    border-radius: 4px;
    padding: 20px;

    .message-detail {
      height: 100%;
      display: flex;
      flex-direction: column;
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .sender-info {
        display: flex;
        gap: 12px;
        align-items: center;

        .sender-text {
          .sender-name {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 4px;
          }

          .send-time {
            font-size: 12px;
            color: var(--color-text-3);
          }
        }
      }
    }

    .detail-content {
      flex: 1;
      overflow-y: auto;

      .message-title {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 16px;
        color: var(--color-text-1);
      }

      .message-body {
        font-size: 14px;
        line-height: 1.8;
        color: var(--color-text-2);
        white-space: pre-wrap;
      }
    }

    .detail-footer {
      display: flex;
      justify-content: flex-end;
    }
  }

</style>