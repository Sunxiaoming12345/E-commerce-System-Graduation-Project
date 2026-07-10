import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let client = null

export function connect(token, onMessage) {
  if (client && client.connected) {
    // 已连接，直接调用回调重订阅（可能因页面切换丢失了回调引用）
    if (onMessage) client.onConnect = () => {}
    return
  }
  const wsUrl = import.meta.env.VITE_WS_URL || '/ws'
  client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    connectHeaders: { Authorization: token },
    debug: () => {},
    onConnect: () => {
      console.log('Admin WebSocket 已连接')
      // 连接建立后再订阅，确保 SUBSCRIBE 帧正常发送
      client.subscribe('/topic/admin', (msg) => {
        try { if (onMessage) onMessage(JSON.parse(msg.body)) } catch {}
      })
    },
    onDisconnect: () => { console.log('Admin WebSocket 已断开') }
  })
  client.activate()
}

export function sendMessage(productId, content, receiverId) {
  if (!client || !client.connected) return
  client.publish({ destination: '/app/chat', body: JSON.stringify({ productId, content, receiverId }) })
}

export function disconnect() {
  if (client) { client.deactivate(); client = null }
}
