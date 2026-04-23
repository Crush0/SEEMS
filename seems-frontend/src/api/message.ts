import axios from 'axios';

export interface MessageRecord {
  id: string;
  type: string;
  title: string;
  subTitle?: string;
  avatar?: string;
  content: string;
  time?: string;
  status: 0 | 1;
  messageType?: number;
  senderUsername?: string;
  senderAvatar?: string;
  senderId?: string;
  createDate?: string;
  isRoleMessage?: boolean;
  role?: string;
}
export type MessageListType = MessageRecord[];

/**
 * 发送消息参数
 */
export interface SendMessageParams {
  receiverId?: string; // 接收者用户ID（与role二选一）
  role?: 'ADMIN' | 'OPERATOR' | 'USER'; // 目标角色组（与receiverId二选一）
  title: string; // 消息标题
  content: string; // 消息内容
  type?: 'message' | 'notice' | 'todo'; // 消息类型
}

/**
 * 查询消息列表参数
 */
export interface QueryMessageListParams {
  type?: 'message' | 'notice' | 'todo'; // 消息类型（可选）
  status?: 0 | 1; // 消息状态（可选）
  current: number; // 当前页码
  pageSize: number; // 每页大小
}

/**
 * 查询消息列表响应
 */
export interface QueryMessageListResponse {
  list: MessageListType;
  total: number;
}

/**
 * 发送消息
 */
export function sendMessage(data: SendMessageParams) {
  return axios.post('/api/message/send', data);
}

/**
 * 查询消息列表
 */
export function queryMessageList(data: QueryMessageListParams) {
  return axios.post<QueryMessageListResponse>('/api/message/list', data);
}

/**
 * 标记消息为已读
 */
export function setMessageStatus(messageIds: string[]) {
  return axios.post('/api/message/read', messageIds);
}

/**
 * 获取未读消息数量
 */
export function getUnreadCount(): Promise<number> {
  return axios.get<number>('/api/message/unread-count').then((res) => res.data);
}

export interface ChatRecord {
  id: number;
  username: string;
  content: string;
  time: string;
  isCollect: boolean;
}

export function queryChatList() {
  return axios.post<ChatRecord[]>('/api/chat/list');
}
