<template>
  <div class="coupons-page">
    <div class="toolbar">
      <el-input v-model="query.name" placeholder="优惠券名称" clearable style="width:200px" @change="loadList" />
      <el-select v-model="query.type" placeholder="类型" clearable style="width:120px" @change="loadList">
        <el-option label="满减" :value="0" />
        <el-option label="折扣" :value="1" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="loadList">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="openAdd">新增优惠券</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="couponId" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 0 ? 'danger' : 'warning'" size="small">
            {{ row.type === 0 ? '满减' : '折扣' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="discountValue" label="优惠值" width="100" />
      <el-table-column prop="minAmount" label="最低消费" width="100" />
      <el-table-column label="发放/已用" width="100">
        <template #default="{ row }">{{ row.totalCount }}/{{ row.usedCount }}</template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column label="来源" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isLottery === 1 ? 'warning' : ''" size="small">
            {{ row.isLottery === 1 ? '抽奖' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next"
        @current-change="loadList" @size-change="loadList" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '新增优惠券'" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="优惠券名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="0">满减</el-radio>
            <el-radio :label="1">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.type === 0 ? '减免金额' : '折扣率'" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" />
          <span v-if="form.type === 1" style="margin-left:8px;color:#999">（0.85 表示85折）</span>
        </el-form-item>
        <el-form-item label="最低消费" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="发放数量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="flex:1;min-width:200px" />
            <el-checkbox v-model="startNow" @change="onStartNow">立即开始</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <div style="display:flex;flex-direction:column;gap:8px;width:100%">
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
            <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
              <el-button size="small" v-for="opt in durationOptions" :key="opt.label" @click="setDuration(opt.hours)">{{ opt.label }}</el-button>
              <el-input-number v-model="customHours" :min="1" :max="9999" size="small" placeholder="自定义" style="width:80px" />
              <span style="font-size:12px;color:#999">小时</span>
              <el-button size="small" @click="setDuration(customHours)">应用</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="类型" prop="isLottery">
          <el-radio-group v-model="form.isLottery">
            <el-radio :label="0">普通优惠券</el-radio>
            <el-radio :label="1">抽奖专享</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponsPage, addCoupon, editCoupon, deleteCoupon } from '@/api/coupons'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const query = reactive({ name: '', type: null, status: null, page: 1, pageSize: 10 })
const form = reactive({
  couponId: null, name: '', type: 0, discountValue: 0, minAmount: 0,
  totalCount: 100, startTime: null, endTime: null, status: 1, isLottery: 0
})
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型' }],
  discountValue: [{ required: true, message: '请输入优惠值' }],
  totalCount: [{ required: true, message: '请输入发放数量' }],
  startTime: [{ required: true, message: '请选择开始时间' }],
  endTime: [{ required: true, message: '请选择结束时间' }]
}

const startNow = ref(false)
const customHours = ref(24)
const durationOptions = [
  { label: '24小时', hours: 24 },
  { label: '72小时', hours: 72 },
  { label: '7天', hours: 168 },
  { label: '15天', hours: 360 },
  { label: '30天', hours: 720 }
]

function formatLocal(d) {
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' '
       + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
}

const lastDurationHours = ref(0)

function onStartNow(val) {
  if (val) form.startTime = formatLocal(new Date())
}

function setDuration(hours) {
  lastDurationHours.value = hours
  const base = (form.startTime && !startNow.value)
    ? new Date(form.startTime.replace(' ', 'T'))
    : new Date()
  base.setHours(base.getHours() + hours)
  form.endTime = formatLocal(base)
}

async function loadList() {
  loading.value = true
  try { const res = await getCouponsPage(query); list.value = res.records; total.value = res.total }
  finally { loading.value = false }
}

function openAdd() { isEdit.value = false; dialogVisible.value = true }
function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    couponId: row.couponId, name: row.name, type: row.type,
    discountValue: row.discountValue, minAmount: row.minAmount,
    totalCount: row.totalCount, startTime: row.startTime, endTime: row.endTime,
    status: row.status, isLottery: row.isLottery
  })
  dialogVisible.value = true
}

function resetForm() {
  Object.assign(form, { couponId: null, name: '', type: 0, discountValue: 0, minAmount: 0, totalCount: 100, startTime: null, endTime: null, status: 1, isLottery: 0 })
  formRef.value?.resetFields()
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  // 提交时重新计算时间，保证开始和结束时间基于同一时刻
  if (startNow.value) {
    const now = new Date()
    form.startTime = formatLocal(now)
    if (lastDurationHours.value > 0) {
      now.setHours(now.getHours() + lastDurationHours.value)
      form.endTime = formatLocal(now)
    }
  }
  if (isEdit.value) { await editCoupon(form); ElMessage.success('修改成功') }
  else { await addCoupon(form); ElMessage.success('新增成功') }
  dialogVisible.value = false; loadList()
}

async function handleRemove(row) {
  await ElMessageBox.confirm('确定删除该优惠券吗？', '提示', { type: 'warning' })
  await deleteCoupon(row.couponId)
  ElMessage.success('删除成功'); loadList()
}

onMounted(loadList)
</script>

<style scoped>
.coupons-page { animation: fade-in 0.25s var(--ease); }
@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }

.toolbar { margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }

/* === Table === */
.coupons-page :deep(.el-table) {
  --el-table-bg-color: #fff; --el-table-tr-bg-color: #fff;
  --el-table-header-bg-color: #fafaf9; --el-table-row-hover-bg-color: #f5f3f0;
  --el-table-border-color: #111; --el-table-text-color: #111;
  --el-table-header-text-color: #111;
  border: 3px solid #111; box-shadow: var(--shadow-hard);
}

.coupons-page :deep(.el-table th.el-table__cell) {
  background: #111; color: #fff;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em;
  border-bottom: 2px solid #111;
}
.coupons-page :deep(.el-table td.el-table__cell) { border-bottom: 2px solid #e5e3e0; }

.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.coupons-page :deep(.el-pager li) { border: 2px solid #111 !important; font-weight: 700 !important; }
.coupons-page :deep(.el-pager li.is-active) { background: #111 !important; color: #fff !important; }

/* === Dialog === */
.coupons-page :deep(.el-dialog) {
  --el-dialog-bg-color: #fff; border: 3px solid #111;
  box-shadow: var(--shadow-hard-lg); border-radius: 0;
}
.coupons-page :deep(.el-dialog__header) { background: #111; padding: 16px 24px; margin: 0; }
.coupons-page :deep(.el-dialog__title) { color: #fff; font-weight: 700; }
.coupons-page :deep(.el-dialog__headerbtn .el-icon) { color: #fff; }
.coupons-page :deep(.el-dialog__body) { padding: 24px; }
.coupons-page :deep(.el-dialog__footer) { border-top: 3px solid #111; padding: 16px 24px; }
</style>
