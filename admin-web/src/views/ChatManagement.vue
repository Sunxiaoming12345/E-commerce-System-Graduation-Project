<template>
  <div class="chat-admin">
    <div class="chat-layout">
      <!-- 会话列表 -->
      <div class="session-list">
        <h3>客服会话</h3>
        <div class="session-card" v-for="s in sessions" :key="s.userId + '-' + s.productId"
             :class="{ active: activeKey === (s.userId + '-' + s.productId) }"
             @click="selectSession(s)">
          <div class="session-info">
            <span class="session-user">用户 #{{ s.userId }}</span>
            <span class="session-product">商品 #{{ s.productId }}</span>
          </div>
          <div class="session-preview">{{ s.lastMessage?.substring(0, 30) || '' }}</div>
          <el-badge v-if="s.unreadCount > 0" :value="s.unreadCount" class="session-badge" />
        </div>
        <el-empty v-if="sessions.length === 0" description="暂无会话" :image-size="80" />
      </div>
      <!-- 聊天窗口 -->
      <div class="chat-main" v-if="active">
        <div class="chat-header">
          <span>用户 #{{ active.userId }} — 商品 #{{ active.productId }}</span>
        </div>
        <div class="chat-body" ref="bodyRef">
          <div v-for="m in messages" :key="m.id" :class="['msg', m.senderType === 1 ? 'msg-self' : 'msg-other']">
            <div class="msg-bubble">{{ m.content }}</div>
            <div class="msg-time">{{ fmtTime(m.createTime) }}</div>
          </div>
          <div v-if="messages.length === 0" class="chat-empty">请选择左侧会话</div>
        </div>
        <div class="chat-input">
          <input v-model="input" @keyup.enter="handleSend" placeholder="输入回复..." />
          <button @click="handleSend" :disabled="!input.trim()">发送</button>
        </div>
      </div>
      <el-empty v-else description="选择左侧会话开始聊天" :image-size="100" class="chat-empty-state" />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { connect, sendMessage, disconnect } from '@/utils/stomp'
import request from '@/utils/request'

const token = localStorage.getItem('admin_token') || ''
let adminId = 1
try {
  const payload = JSON.parse(atob(token.split('.')[1]))
  adminId = Number(payload.id || payload.userId || payload.adminId || 1)
} catch {}

const sessions = ref([])
const messages = ref([])
const input = ref('')
const active = ref(null)
const activeKey = ref('')
const bodyRef = ref(null)

function nowLocal() {
  const d = new Date()
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth()+1) + '-' + pad(d.getDate()) + ' '
       + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
}

function fmtTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

async function loadSessions() {
  try {
    const data = await request.get(`/admin/chat/sessions?adminId=${adminId}`)
    sessions.value = Array.isArray(data) ? data : []
  } catch {}
}

async function loadHistory() {
  if (!active.value) return
  try {
    const data = await request.get(`/user/chat/history?productId=${active.value.productId}&userId=${active.value.userId}&adminId=${adminId}`)
    messages.value = Array.isArray(data) ? data : []
    scrollBottom()
  } catch {}
}

function scrollBottom() {
  nextTick(() => { if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight })
}

function selectSession(s) {
  active.value = s
  activeKey.value = s.userId + '-' + s.productId
  loadHistory()
}

function handleNewMessage(msg) {
  // 收到用户消息，刷新会话列表
  loadSessions()
  // 如果正在看这个会话，追加到消息列表
  if (active.value && active.value.userId === msg.senderId && active.value.productId === msg.productId) {
    messages.value.push(msg)
    scrollBottom()
  }
}

function handleSend() {
  if (!input.value.trim() || !active.value) return
  sendMessage(active.value.productId, input.value.trim(), active.value.userId)
  messages.value.push({
    id: Date.now(), senderId: adminId, senderType: 1,
    productId: active.value.productId, content: input.value.trim(),
    createTime: nowLocal()
  })
  scrollBottom()
  input.value = ''
}

onMounted(() => {
  loadSessions()
  connect(token, handleNewMessage)
})

onUnmounted(() => disconnect())
</script>

<style scoped>
.chat-admin { height: calc(100vh - 120px); display: flex; flex-direction: column; }
.chat-layout { display: flex; flex: 1; overflow: hidden; gap: 0; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; }
.session-list { width: 280px; border-right: 1px solid #e5e7eb; overflow-y: auto; padding: 12px; }
.session-list h3 { font-size: 16px; margin-bottom: 12px; }
.session-card {
  position: relative; padding: 12px; border-radius: 8px; cursor: pointer; margin-bottom: 8px;
  border: 1px solid #f1f5f9; transition: .15s;
}
.session-card:hover, .session-card.active { background: #eff6ff; border-color: #93c5fd; }
.session-info { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px; }
.session-product { color: #64748b; }
.session-preview { font-size: 12px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.session-badge { position: absolute; top: 8px; right: 8px; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-header { padding: 12px 16px; background: #f8fafc; border-bottom: 1px solid #e5e7eb; font-weight: 600; }
.chat-body { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.chat-empty, .chat-empty-state { flex: 1; display: flex; align-items: center; justify-content: center; color: #999; }
.msg { max-width: 70%; }
.msg-self { align-self: flex-end; }
.msg-other { align-self: flex-start; }
.msg-bubble { padding: 10px 14px; border-radius: 16px; font-size: 14px; line-height: 1.5; word-break: break-all; }
.msg-self .msg-bubble { background: #2563eb; color: #fff; border-bottom-right-radius: 4px; }
.msg-other .msg-bubble { background: #f1f5f9; color: #334155; border-bottom-left-radius: 4px; }
.msg-time { font-size: 11px; color: #94a3b8; margin-top: 4px; }
.msg-self .msg-time { text-align: right; }
.chat-input { display: flex; padding: 12px; border-top: 1px solid #e5e7eb; gap: 8px; }
.chat-input input { flex: 1; padding: 10px 14px; border: 1px solid #e5e7eb; border-radius: 8px; outline: none; }
.chat-input button { padding: 10px 20px; border: none; border-radius: 8px; background: #2563eb; color: #fff; cursor: pointer; }
.chat-input button:disabled { background: #cbd5e1; cursor: not-allowed; }
</style>
