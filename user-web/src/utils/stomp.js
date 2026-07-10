import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let client = null
let subscribedTopics = new Set()

export function connect(token, onMessage, onConnected) {
  if (client && client.connected) {
    if (onConnected) onConnected()
    return
  }
  const wsUrl = import.meta.env.VITE_WS_URL || '/ws'
  client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    connectHeaders: { Authorization: token },
    debug: () => {},
    onConnect: () => {
      console.log('WebSocket 已连接')
      if (onConnected) onConnected()
    },
    onDisconnect: () => { console.log('WebSocket 已断开') },
    onStompError: (frame) => { console.error('STOMP 错误:', frame.headers['message']) }
  })
  client.activate()
}

let topicCb = null
export function subscribeUserMessages(userId, cb) {
  if (!client) return
  const topic = `/topic/user.${userId}`
  if (subscribedTopics.has(topic)) return
  const sub = client.subscribe(topic, (msg) => {
    try { cb(JSON.parse(msg.body)) } catch { cb(msg.body) }
  })
  subscribedTopics.add(topic)
  topicCb = { sub, topic }
}

let adminCb = null
export function subscribeAdminTopic(cb) {
  if (!client) return
  const topic = '/topic/admin'
  if (subscribedTopics.has(topic)) return
  const sub = client.subscribe(topic, (msg) => {
    try { cb(JSON.parse(msg.body)) } catch { cb(msg.body) }
  })
  subscribedTopics.add(topic)
  adminCb = { sub, topic }
}

export function sendMessage(productId, content) {
  if (!client || !client.connected) return
  client.publish({ destination: '/app/chat', body: JSON.stringify({ productId, content }) })
}

export function disconnect() {
  if (client) { client.deactivate(); client = null }
  subscribedTopics.clear()
}
