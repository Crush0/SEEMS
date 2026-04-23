import { io } from 'socket.io-client';
import { setSocketHandler } from './handler';

export const URL = import.meta.env.VITE_SOCKET_BASE_URL;

let socketInstance: any = null;

export function initSocket(auth: string) {
  // 如果已存在socket实例，先断开
  if (socketInstance) {
    socketInstance.disconnect();
    socketInstance = null;
  }

  socketInstance = io(`${URL}?token=${auth}`, {
    transports: ['websocket'],
    upgrade: false,
    reconnection: true,
    reconnectionDelay: 1000,
    reconnectionAttempts: 5,
    timeout: 10000,
  });

  socketInstance.on('connect', () => {
    console.log('Socket connected');
    setSocketHandler(socketInstance);
  });

  socketInstance.on('disconnect', (reason) => {
    console.log('Socket disconnected:', reason);
    // 只在非主动断开时重连
    if (reason !== 'io client disconnect') {
      // Socket.IO会自动重连
    }
  });

  socketInstance.on('connect_error', (error) => {
    console.error('Socket connection error:', error);
  });

  return socketInstance;
}

export function getSocket() {
  return socketInstance;
}

export function closeSocket() {
  if (socketInstance) {
    socketInstance.disconnect();
    socketInstance = null;
  }
}
