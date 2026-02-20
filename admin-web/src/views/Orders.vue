<template>
  <div class="orders-page">
    <div class="toolbar">
      <el-input v-model="query.orderNumber" placeholder="订单号" clearable style="width: 180px" />
      <el-select v-model="query.orderStatus" placeholder="订单状态" clearable style="width: 120px">
        <el-option label="待付款" :value="0" />
        <el-option label="已付款" :value="1" />
        <el-option label="待发货" :value="2" />
        <el-option label="已发货" :value="3" />
        <el-option label="已完成" :value="4" />
        <el-option label="已取消" :value="5" />
      </el-select>
      <el-button type="primary" @click="loadList">查询</el-button>
    </div>
    <el-table :data="list" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNumber" label="订单号" min-width="160" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥ {{ formatMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="状态" width="90">
        <template #default="{ row }">{{ orderStatusText(row.orderStatus) }}</template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="电话" width="120" />
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDetail(row)">详情</el-button>
          <el-button type="primary" link @click="openStatus(row)">改状态</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <template v-if="orderDetail.order">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.order.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ orderStatusText(orderDetail.order.orderStatus) }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥ {{ formatMoney(orderDetail.order.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderDetail.order.receiverName }} {{ orderDetail.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ orderDetail.order.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ orderDetail.order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ orderDetail.order.payTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-items">
          <div class="detail-title">商品明细</div>
          <el-table :data="orderDetail.orderItems || []" size="small" border>
            <el-table-column prop="productName" label="商品" />
            <el-table-column prop="productPrice" label="单价" width="100">
              <template #default="{ row }">¥ {{ formatMoney(row.productPrice) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="subtotal" label="小计" width="100">
              <template #default="{ row }">¥ {{ formatMoney(row.subtotal) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" title="修改订单状态" width="400px">
      <el-form label-width="90px">
        <el-form-item label="订单号">{{ currentOrder?.orderNumber }}</el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusForm.orderStatus" placeholder="请选择" style="width: 100%">
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="已发货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="statusForm.remark" type="textarea" rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrdersPage, getOrderDetail, updateOrderStatus } from '@/api/orders'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ orderNumber: '', userId: null, orderStatus: null, page: 1, pageSize: 10 })

const detailVisible = ref(false)
const orderDetail = ref({ order: null, orderItems: [], payment: null })

const statusVisible = ref(false)
const currentOrder = ref(null)
const statusForm = reactive({ orderId: null, orderStatus: null, remark: '' })

const statusMap = { 0: '待付款', 1: '已付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }
function orderStatusText(s) {
  return statusMap[s] ?? '-'
}

function formatMoney(v) {
  if (v == null) return '0.00'
  return Number(v).toFixed(2)
}

async function loadList() {
  loading.value = true
  try {
    const res = await getOrdersPage({
      orderNumber: query.orderNumber || undefined,
      userId: query.userId ?? undefined,
      orderStatus: query.orderStatus ?? undefined,
      page: query.page,
      pageSize: query.pageSize
    })
    list.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  try {
    orderDetail.value = await getOrderDetail(row.orderId)
  } catch (e) {
    orderDetail.value = {}
  }
  detailVisible.value = true
}

function openStatus(row) {
  currentOrder.value = row
  statusForm.orderId = row.orderId
  statusForm.orderStatus = row.orderStatus
  statusForm.remark = ''
  statusVisible.value = true
}

async function submitStatus() {
  await updateOrderStatus({ orderId: statusForm.orderId, orderStatus: statusForm.orderStatus, remark: statusForm.remark })
  ElMessage.success('状态已更新')
  statusVisible.value = false
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.orders-page .toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
.orders-page .pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.detail-items {
  margin-top: 16px;
}
.detail-title {
  margin-bottom: 8px;
  font-weight: 600;
  color: #1a202c;
}
</style>
