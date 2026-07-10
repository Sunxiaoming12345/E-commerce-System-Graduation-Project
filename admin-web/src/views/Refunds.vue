<template>
  <div class="refunds-page">
    <div class="toolbar">
      <el-input v-model="query.orderId" placeholder="订单ID" clearable style="width:150px" @change="loadList" />
      <el-input v-model="query.userId" placeholder="用户ID" clearable style="width:150px" @change="loadList" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="loadList">
        <el-option label="待处理" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="已完成" :value="3" />
      </el-select>
    </div>

    <el-table :data="list" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="refundId" label="ID" width="80" />
      <el-table-column prop="orderId" label="订单ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="amount" label="退款金额" width="110" />
      <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminRemark" label="管理员备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" link @click="openApprove(row)">处理</el-button>
          <span v-else style="color:#999">-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next"
        @current-change="loadList" @size-change="loadList" />
    </div>

    <el-dialog v-model="dialogVisible" title="处理退款" width="500px">
      <div v-if="currentRefund" style="margin-bottom:16px">
        <p><b>退款ID：</b>{{ currentRefund.refundId }}</p>
        <p><b>订单ID：</b>{{ currentRefund.orderId }}</p>
        <p><b>金额：</b>¥{{ currentRefund.amount }}</p>
        <p><b>原因：</b>{{ currentRefund.reason }}</p>
      </div>
      <el-form ref="approveFormRef" :model="approveForm" label-width="80px">
        <el-form-item label="操作">
          <el-radio-group v-model="approveForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="approveForm.adminRemark" type="textarea" placeholder="备注（可选）" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRefundsPage, approveRefund } from '@/api/refunds'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const currentRefund = ref(null)
const query = reactive({ orderId: null, userId: null, status: null, page: 1, pageSize: 10 })
const approveForm = reactive({ refundId: null, approved: true, adminRemark: '' })

function statusText(s) { return ['待处理', '已通过', '已拒绝', '已完成'][s] || '' }
function statusType(s) { return ['warning', 'success', 'danger', 'info'][s] || 'info' }

async function loadList() {
  loading.value = true
  try { const res = await getRefundsPage(query); list.value = res.records; total.value = res.total }
  finally { loading.value = false }
}

function openApprove(row) {
  currentRefund.value = row
  approveForm.refundId = row.refundId
  approveForm.approved = true
  approveForm.adminRemark = ''
  dialogVisible.value = true
}

async function submitApprove() {
  await approveRefund(approveForm)
  ElMessage.success('处理完成')
  dialogVisible.value = false
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.refunds-page { animation: fade-in 0.25s var(--ease); }
@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }

.toolbar { margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap; }

/* === Table === */
.refunds-page :deep(.el-table) {
  --el-table-bg-color: #fff; --el-table-tr-bg-color: #fff;
  --el-table-header-bg-color: #fafaf9; --el-table-row-hover-bg-color: #f5f3f0;
  --el-table-border-color: #111; --el-table-text-color: #111;
  --el-table-header-text-color: #111;
  border: 3px solid #111; box-shadow: var(--shadow-hard);
}

.refunds-page :deep(.el-table th.el-table__cell) {
  background: #111; color: #fff;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em;
  border-bottom: 2px solid #111;
}
.refunds-page :deep(.el-table td.el-table__cell) { border-bottom: 2px solid #e5e3e0; }

.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.refunds-page :deep(.el-pager li) { border: 2px solid #111 !important; font-weight: 700 !important; }
.refunds-page :deep(.el-pager li.is-active) { background: #111 !important; color: #fff !important; }

/* === Dialog === */
.refunds-page :deep(.el-dialog) {
  --el-dialog-bg-color: #fff; border: 3px solid #111;
  box-shadow: var(--shadow-hard-lg); border-radius: 0;
}
.refunds-page :deep(.el-dialog__header) { background: #111; padding: 16px 24px; margin: 0; }
.refunds-page :deep(.el-dialog__title) { color: #fff; font-weight: 700; }
.refunds-page :deep(.el-dialog__headerbtn .el-icon) { color: #fff; }
.refunds-page :deep(.el-dialog__body) { padding: 24px; }
.refunds-page :deep(.el-dialog__footer) { border-top: 3px solid #111; padding: 16px 24px; }

.refunds-page p { color: var(--text-secondary); margin-bottom: 6px; }
.refunds-page b { color: #111; }
</style>
