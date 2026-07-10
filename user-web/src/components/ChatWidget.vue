<template>
  <div class="chat-widget">
    <button class="chat-toggle" @click="toggle">
      <span v-if="!open">💬 联系客服</span>
      <span v-else>✕ 关闭</span>
      <span v-if="unread > 0 && !open" class="chat-badge">{{ unread }}</span>
    </button>
    <div class="chat-panel" v-if="open">
      <div class="chat-header">{{ productName }} - 客服</div>
      <div class="chat-body" ref="bodyRef">
        <div v-for="m in messages" :key="m.id" :class="['msg', m.senderType === 0 ? 'msg-user' : 'msg-admin']">
          <div class="msg-bubble">{{ m.content }}</div>
          <div class="msg-time">{{ fmtTime(m.createTime) }}</div>
        </div>
        <div v-if="messages.length === 0" class="chat-empty">暂无消息，发送第一条消息吧</div>
      </div>
      <div class="chat-input">
        <input v-model="input" @keyup.enter="handleSend" placeholder="输入消息..." />
        <button @click="handleSend" :disabled="!input.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, onUnmounted } from 'vue'
import { connect, subscribeUserMessages, sendMessage, disconnect } from '@/utils/stomp'
import request from '@/utils/request'

const props = defineProps({ productId: Number, productName: String })
const open = ref(false)
const input = ref('')
const messages = ref([])
const unread = ref(0)
const bodyRef = ref(null)

const token = localStorage.getItem('user_token') || ''
// 从 token payload 取 userId
let userId = 0
try {
  const payload = JSON.parse(atob(token.split('.')[1]))
  userId = Number(payload.id || payload.userId || 0)
} catch {}

function nowLocal() {
  const d = new Date()
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth()+1) + '-' + pad(d.getDate()) + ' '
       + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
}

function fmtTime(t) {
  if (!t) return ''
  if (typeof t === 'string') return t.replace('T', ' ').substring(0, 19)
  return nowLocal()
}

async function loadHistory() {
  try {
    const data = await request.get(`/user/chat/history?productId=${props.productId}&userId=${userId}&adminId=1`)
    messages.value = Array.isArray(data) ? data : []
    scrollBottom()
  } catch { /* */ }
}

function scrollBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

function toggle() {
  open.value = !open.value
  if (open.value) {
    unread.value = 0
    connect(token, (msg) => {
      messages.value.push(msg)
      scrollBottom()
      if (!open.value) unread.value++
    }, () => {
      subscribeUserMessages(userId, (msg) => {
        messages.value.push(msg)
        scrollBottom()
        if (!open.value) unread.value++
      })
    })
    if (messages.value.length === 0) loadHistory()
  }
}

function handleSend() {
  if (!input.value.trim()) return
  sendMessage(props.productId, input.value.trim())
  messages.value.push({
    id: Date.now(),
    senderId: userId,
    senderType: 0,
    productId: props.productId,
    content: input.value.trim(),
    createTime: nowLocal()
  })
  scrollBottom()
  input.value = ''
}

onUnmounted(() => disconnect())
</script>

<style scoped>
.chat-widget { position: fixed; bottom: 24px; right: 24px; z-index: 999; font-family: system-ui, sans-serif; }
.chat-toggle {
  position: relative; padding: 12px 24px; border: none; border-radius: 50px;
  background: #2563eb; color: #fff; font-size: 15px; cursor: pointer;
  box-shadow: 0 4px 16px rgba(37,99,235,.4); transition: .2s;
}
.chat-toggle:hover { transform: translateY(-2px); }
.chat-badge {
  position: absolute; top: -6px; right: -6px; background: #ef4444; color: #fff;
  border-radius: 50%; width: 22px; height: 22px; font-size: 12px; line-height: 22px; text-align: center;
}
.chat-panel {
  position: absolute; bottom: 60px; right: 0; width: 360px; height: 480px;
  background: #fff; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,.15);
  display: flex; flex-direction: column; overflow: hidden;
}
.chat-header { padding: 14px 16px; background: #2563eb; color: #fff; font-weight: 600; font-size: 15px; }
.chat-body { flex: 1; overflow-y: auto; padding: 12px; display: flex; flex-direction: column; gap: 10px; }
.chat-empty { text-align: center; color: #999; margin-top: 60px; font-size: 14px; }
.msg { max-width: 80%; }
.msg-user { align-self: flex-end; }
.msg-admin { align-self: flex-start; }
.msg-bubble { padding: 10px 14px; border-radius: 18px; font-size: 14px; line-height: 1.5; word-break: break-all; }
.msg-user .msg-bubble { background: #2563eb; color: #fff; border-bottom-right-radius: 4px; }
.msg-admin .msg-bubble { background: #f1f5f9; color: #334155; border-bottom-left-radius: 4px; }
.msg-time { font-size: 11px; color: #94a3b8; margin-top: 4px; }
.msg-user .msg-time { text-align: right; }
.chat-input { display: flex; padding: 10px; border-top: 1px solid #e5e7eb; gap: 8px; }
.chat-input input { flex: 1; padding: 10px 14px; border: 1px solid #e5e7eb; border-radius: 20px; outline: none; font-size: 14px; }
.chat-input button { padding: 10px 18px; border: none; border-radius: 20px; background: #2563eb; color: #fff; cursor: pointer; font-size: 14px; }
.chat-input button:disabled { background: #cbd5e1; cursor: not-allowed; }
</style>
