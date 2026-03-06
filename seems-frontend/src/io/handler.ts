// import { RealTimeData } from '@/store/modules/ship-data/type';
import mitt from 'mitt';
import { Socket } from 'socket.io-client';

export const socketEmitter = mitt();

export const SocketEvent = {
  REALTIME: 'realtime',
};

export const setSocketHandler = (socket: Socket) => {
  socket.on(SocketEvent.REALTIME, (data: any) => {
    socketEmitter.emit(SocketEvent.REALTIME, data);
  });
};
