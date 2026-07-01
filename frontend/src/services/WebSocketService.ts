import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = 'http://localhost:8080/ws';

export class WebSocketService {
  private client: Client;

  constructor(
    onConnect: () => void,
    onDisconnect: () => void,
    onTrade: (trade: any) => void,
    onDepth: (depth: any) => void
  ) {
    this.client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('Connected to HFT STOMP Server');
        onConnect();

        this.client.subscribe('/topic/trades', (msg: IMessage) => {
          if (msg.body) onTrade(JSON.parse(msg.body));
        });

        this.client.subscribe('/topic/depth', (msg: IMessage) => {
          if (msg.body) onDepth(JSON.parse(msg.body));
        });
      },
      onWebSocketClose: () => {
        console.warn('Disconnected. Attempting to reconnect...');
        onDisconnect();
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        onDisconnect();
      }
    });
  }

  public connect() {
    this.client.activate();
  }

  public disconnect() {
    this.client.deactivate();
  }
}
