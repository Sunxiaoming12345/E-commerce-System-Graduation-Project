<template>
  <div class="seckill-admin">
    <div class="page-header">
      <h2>⚡ 秒杀管理</h2>
      <el-button type="primary" @click="openDialog()">新建秒杀</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="productId" label="商品ID" width="80" />
      <el-table-column prop="productName" label="商品名称" min-width="150" />
      <el-table-column prop="seckillPrice" label="秒杀价" width="100" />
      <el-table-column label="库存" width="120">
        <template #default="{ row }">{{ row.stock }} / {{ row.originStock }}</template>
      </el-table-column>
      <el-table-column label="开始时间" width="160">
        <template #default="{ row }">{{ fmt(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="160">
        <template #default="{ row }">{{ fmt(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="warning">未开始</el-tag>
          <el-tag v-else-if="row.status === 2" type="danger">进行中</el-tag>
          <el-tag v-else type="info">已结束</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑秒杀' : '新建秒杀'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品ID">
          <div style="display:flex;gap:8px">
            <el-input-number v-model="form.productId" :min="1" style="flex:1" />
            <el-button @click="fetchProduct" :loading="searchLoading">查找</el-button>
          </div>
        </el-form-item>
        <el-form-item label="商品名称" v-if="form.productName">
          <span class="readonly-text">{{ form.productName }}</span>
        </el-form-item>
        <el-form-item label="秒杀价格">
          <el-input-number v-model="form.seckillPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="秒杀库存">
          <el-input-number v-model="form.stock" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间">
          <div style="display:flex;gap:12px;align-items:center">
            <el-date-picker v-model="form.startTime" type="datetime"
              format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择开始时间" style="flex:1" />
            <el-checkbox v-model="startNow" @change="onStartNow">立即开始</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="结束时间">
          <div style="display:flex;flex-direction:column;gap:8px;width:100%">
            <el-date-picker v-model="form.endTime" type="datetime"
              format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择结束时间" />
            <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
              <el-button size="small" v-for="opt in durationOptions" :key="opt.label" @click="setDuration(opt.hours)">{{ opt.label }}</el-button>
              <el-input-number v-model="customHours" :min="1" size="small" style="width:70px" />
              <span style="font-size:12px;color:#999">小时</span>
              <el-button size="small" @click="setDuration(customHours)">应用</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({ id: null, productId: null, productName: '', productImage: '', seckillPrice: 1, stock: 100, startTime: '', endTime: '' })
const searchLoading = ref(false)
const startNow = ref(false)
const customHours = ref(24)
const durationOptions = [
  { label: '1小时', hours: 1 }, { label: '2小时', hours: 2 },
  { label: '6小时', hours: 6 }, { label: '12小时', hours: 12 },
  { label: '24小时', hours: 24 }
]

function fmt(t) { return t ? t.replace('T', ' ').substring(0, 19) : '' }

function pad(n) { return String(n).padStart(2, '0') }
function formatLocal(d) {
  return d.getFullYear() + '-' + pad(d.getMonth()+1) + '-' + pad(d.getDate()) + ' '
       + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
}

function onStartNow(val) {
  if (val) form.value.startTime = formatLocal(new Date())
}

function setDuration(hours) {
  const base = (form.value.startTime && !startNow.value)
    ? new Date(form.value.startTime.replace(' ', 'T'))
    : new Date()
  base.setHours(base.getHours() + hours)
  form.value.endTime = formatLocal(base)
}

async function load() {
  try {
    const data = await request.get('/admin/seckill/list')
    list.value = Array.isArray(data) ? data : []
  } catch {}
}

async function fetchProduct() {
  if (!form.value.productId) return
  searchLoading.value = true
  try {
    const data = await request.get(`/products/products/${form.value.productId}`)
    if (data) {
      form.value.productName = data.name || ''
      form.value.productImage = data.imageUrl || ''
    }
  } catch {
    form.value.productName = ''
    ElMessage.error('未找到该商品')
  }
  searchLoading.value = false
}

function openDialog(row) {
  startNow.value = false
  if (row) {
    form.value = {
      id: row.id, productId: row.productId, productName: row.productName,
      productImage: row.productImage, seckillPrice: row.seckillPrice,
      stock: row.stock, startTime: row.startTime, endTime: row.endTime
    }
  } else {
    form.value = { id: null, productId: null, productName: '', productImage: '', seckillPrice: 1, stock: 100, startTime: '', endTime: '' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const payload = { ...form.value }
    if (form.value.id) {
      await request.put(`/admin/seckill/${form.value.id}`, payload)
    } else {
      await request.post('/admin/seckill/create', payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await request.delete(`/admin/seckill/${id}`)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.seckill-admin { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; }
.readonly-text { color: #666; }
</style>
