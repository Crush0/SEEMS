import { io } from 'socket.io-client';
import { setSocketHandler } from './handler';

export const URL = import.meta.env.VITE_SOCKET_BASE_URL;

export function initSocket(auth: string) {
  let socket = io(`${URL}?token=${auth}`, {
    transports: ['websocket'],
    upgrade: false,
    reconnection: true,
  });
  socket.on('connect', () => {
    setSocketHandler(socket);
  });
  socket.on('disconnect', () => {
    // 重连
    socket.io.opts.transports = ['websocket'];
    socket.removeAllListeners();
    socket = io(`${URL}?token=${auth}`, {
      transports: ['websocket'],
      upgrade: false,
      reconnection: true,
    });
  });
  socket.on('connect_error', () => {
    // 重连
    socket.io.opts.transports = ['websocket'];
    socket.removeAllListeners();
    socket = io(`${URL}?token=${auth}`, {
      transports: ['websocket'],
      upgrade: false,
      reconnection: true,
    });
  });
  return socket;
}
