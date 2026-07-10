<template>
  <div class="lottery-admin">
    <h2 class="page-heading">抽奖管理</h2>

    <el-tabs v-model="activeTab">
      <!-- Tab 1: 抽奖配置 -->
      <el-tab-pane label="抽奖配置" name="config">
        <div class="tab-body">
          <el-form :model="configForm" label-width="180px" class="config-form">
            <el-form-item label="每次抽奖消耗(元)">
              <el-input-number v-model="configForm.spinCost" :min="0.01" :precision="2" />
            </el-form-item>
            <el-form-item label="每日抽奖次数限制">
              <el-input-number v-model="configForm.dailyLimit" :min="0" :step="1" />
              <span style="margin-left:8px;color:#999;font-size:12px">0 = 不限制</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 奖池管理 -->
      <el-tab-pane label="奖池管理" name="prizes">
        <div class="tab-body">
          <div class="toolbar">
            <el-button type="primary" @click="openPrizeDialog()">新增奖品</el-button>
          </div>
          <el-table :data="prizes" stripe v-loading="prizesLoading" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="prizeName" label="名称" min-width="120" />
            <el-table-column prop="prizeType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="typeTag(row.prizeType)" size="small">{{ typeLabel(row.prizeType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="probability" label="概率" width="80">
              <template #default="{ row }">{{ (row.probability * 100).toFixed(2) }}%</template>
            </el-table-column>
            <el-table-column label="库存" width="120">
              <template #default="{ row }">
                <template v-if="row.prizeType === 'PHYSICAL'">{{ row.remainingStock }}/{{ row.totalStock }}</template>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPrizeDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeletePrize(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- Tab 3: 抽奖记录 -->
      <el-tab-pane label="抽奖记录" name="records">
        <div class="tab-body">
          <el-table :data="records" stripe v-loading="recordsLoading" border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="prizeName" label="奖品" min-width="120" />
            <el-table-column prop="prizeType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="typeTag(row.prizeType)" size="small">{{ typeLabel(row.prizeType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="spinCost" label="消耗" width="80">
              <template #default="{ row }">¥{{ row.spinCost }}</template>
            </el-table-column>
            <el-table-column label="发货状态" width="110">
              <template #default="{ row }">
                <template v-if="row.prizeType === 'PHYSICAL'">
                  <el-tag :type="fulfillmentTag(row.fulfillmentStatus)" size="small">
                    {{ fulfillmentLabel(row.fulfillmentStatus) }}
                  </el-tag>
                </template>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ row.createTime?.replace('T', ' ').substring(0, 19) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="row.prizeType === 'PHYSICAL' && row.fulfillmentStatus < 2"
                  link type="primary" @click="openFulfillmentDialog(row)">发货</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="recQuery.page"
            v-model:page-size="recQuery.pageSize"
            :total="recTotal" layout="total, prev, pager, next"
            @change="loadRecords"
            style="margin-top:16px;justify-content:flex-end"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 奖品弹窗 -->
    <el-dialog v-model="prizeDialogVisible" :title="prizeEditMode ? '编辑奖品' : '新增奖品'" width="480px" destroy-on-close>
      <el-form ref="prizeFormRef" :model="prizeForm" :rules="prizeRules" label-width="100px">
        <el-form-item label="奖品名称" prop="prizeName">
          <el-input v-model="prizeForm.prizeName" placeholder="如：满100减20优惠券" />
        </el-form-item>
        <el-form-item label="奖品类型" prop="prizeType">
          <el-select v-model="prizeForm.prizeType" placeholder="选择类型" @change="onPrizeTypeChange">
            <el-option label="优惠券" value="COUPON" />
            <el-option label="余额" value="BALANCE" />
            <el-option label="谢谢参与" value="THANKS" />
            <el-option label="实物大奖" value="PHYSICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖品图片" prop="prizeImage">
          <el-input v-model="prizeForm.prizeImage" placeholder="图片URL" />
        </el-form-item>
        <el-form-item v-if="prizeForm.prizeType === 'COUPON'" label="关联优惠券ID" prop="couponId">
          <el-input-number v-model="prizeForm.couponId" :min="1" placeholder="coupons表id" />
        </el-form-item>
        <el-form-item v-if="prizeForm.prizeType === 'BALANCE'" label="余额金额" prop="balanceAmount">
          <el-input-number v-model="prizeForm.balanceAmount" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item v-if="prizeForm.prizeType === 'PHYSICAL'" label="总库存" prop="totalStock">
          <el-input-number v-model="prizeForm.totalStock" :min="1" />
        </el-form-item>
        <el-form-item v-if="prizeForm.prizeType === 'PHYSICAL'" label="剩余库存" prop="remainingStock">
          <el-input-number v-model="prizeForm.remainingStock" :min="0" :max="prizeForm.totalStock || 99999" />
        </el-form-item>
        <el-form-item label="概率" prop="probability">
          <el-input-number v-model="prizeForm.probability" :min="0" :max="1" :precision="4" :step="0.05" />
          <span style="margin-left:8px;color:#999;font-size:12px">0~1，所有奖品概率之和应≈1</span>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="prizeForm.statusBool" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="prizeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePrize">保存</el-button>
      </template>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog v-model="fulfillmentVisible" title="实物发货" width="400px" destroy-on-close>
      <el-form :model="fulfillmentForm" label-width="100px">
        <el-form-item label="奖品">{{ fulfillmentForm.prizeName }}</el-form-item>
        <el-form-item label="发货状态">
          <el-select v-model="fulfillmentForm.fulfillmentStatus">
            <el-option label="待发货" :value="0" />
            <el-option label="已发货" :value="1" />
            <el-option label="已收货" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流信息">
          <el-input v-model="fulfillmentForm.shippingInfo" placeholder="快递单号/物流公司" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fulfillmentVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFulfillment">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as lotteryApi from '@/api/lottery'

const activeTab = ref('config')

// --- 配置 ---
const configForm = reactive({ spinCost: 10, dailyLimit: 0 })
async function loadConfig() {
  try {
    const cfg = await lotteryApi.getConfig()
    configForm.spinCost = cfg.spinCost ?? 10
    configForm.dailyLimit = cfg.dailyLimit ?? 0
  } catch { /* */ }
}
async function saveConfig() {
  try {
    await lotteryApi.updateConfig({ spinCost: configForm.spinCost, dailyLimit: configForm.dailyLimit })
    ElMessage.success('配置已保存')
  } catch (e) { ElMessage.error(e.message || '保存失败') }
}

// --- 奖池 ---
const prizes = ref([])
const prizesLoading = ref(false)
const prizeDialogVisible = ref(false)
const prizeEditMode = ref(false)
const prizeFormRef = ref(null)
const prizeForm = reactive({
  id: null, prizeType: 'COUPON', prizeName: '', prizeImage: '',
  couponId: null, balanceAmount: null, totalStock: null, remainingStock: null,
  probability: 0.1, statusBool: true
})
const prizeRules = {
  prizeName: [{ required: true, message: '请输入奖品名称', trigger: 'blur' }],
  prizeType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

async function loadPrizes() {
  prizesLoading.value = true
  try { prizes.value = await lotteryApi.getPrizes() || [] } catch { /* */ }
  prizesLoading.value = false
}
function onPrizeTypeChange() {
  prizeForm.couponId = null
  prizeForm.balanceAmount = null
  prizeForm.totalStock = null
  prizeForm.remainingStock = null
}
function openPrizeDialog(row) {
  if (row) {
    prizeEditMode.value = true
    Object.assign(prizeForm, {
      id: row.id, prizeType: row.prizeType, prizeName: row.prizeName,
      prizeImage: row.prizeImage, couponId: row.couponId,
      balanceAmount: row.balanceAmount, totalStock: row.totalStock,
      remainingStock: row.remainingStock,
      probability: row.probability, statusBool: row.status === 1
    })
  } else {
    prizeEditMode.value = false
    Object.assign(prizeForm, {
      id: null, prizeType: 'COUPON', prizeName: '', prizeImage: '',
      couponId: null, balanceAmount: null, totalStock: null,
      probability: 0.1, statusBool: true
    })
  }
  prizeDialogVisible.value = true
}
async function savePrize() {
  await prizeFormRef.value?.validate().catch(() => {})
  const data = {
    prizeType: prizeForm.prizeType, prizeName: prizeForm.prizeName,
    prizeImage: prizeForm.prizeImage, probability: prizeForm.probability,
    status: prizeForm.statusBool ? 1 : 0,
    couponId: prizeForm.prizeType === 'COUPON' ? prizeForm.couponId : null,
    balanceAmount: prizeForm.prizeType === 'BALANCE' ? prizeForm.balanceAmount : null,
    totalStock: prizeForm.prizeType === 'PHYSICAL' ? prizeForm.totalStock : null,
    remainingStock: prizeForm.prizeType === 'PHYSICAL' ? prizeForm.remainingStock : null,
  }
  try {
    if (prizeEditMode.value) {
      await lotteryApi.editPrize({ id: prizeForm.id, ...data })
      ElMessage.success('编辑成功')
    } else {
      await lotteryApi.addPrize(data)
      ElMessage.success('新增成功')
    }
    prizeDialogVisible.value = false
    loadPrizes()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}
async function handleDeletePrize(id) {
  try {
    await ElMessageBox.confirm('确定删除该奖品？', '提示', { type: 'warning' })
    await lotteryApi.deletePrize(id)
    ElMessage.success('已删除')
    loadPrizes()
  } catch { /* cancelled */ }
}

// --- 记录 ---
const records = ref([])
const recordsLoading = ref(false)
const recQuery = reactive({ page: 1, pageSize: 10 })
const recTotal = ref(0)
async function loadRecords() {
  recordsLoading.value = true
  try {
    const res = await lotteryApi.getRecords(recQuery)
    records.value = res?.records || []
    recTotal.value = res?.total || 0
  } catch { /* */ }
  recordsLoading.value = false
}

// --- 发货 ---
const fulfillmentVisible = ref(false)
const fulfillmentForm = reactive({ recordId: null, prizeName: '', fulfillmentStatus: 0, shippingInfo: '' })
function openFulfillmentDialog(row) {
  Object.assign(fulfillmentForm, {
    recordId: row.id, prizeName: row.prizeName,
    fulfillmentStatus: row.fulfillmentStatus ?? 0, shippingInfo: row.shippingInfo || ''
  })
  fulfillmentVisible.value = true
}
async function saveFulfillment() {
  try {
    await lotteryApi.updateFulfillment({
      recordId: fulfillmentForm.recordId,
      fulfillmentStatus: fulfillmentForm.fulfillmentStatus,
      shippingInfo: fulfillmentForm.shippingInfo
    })
    ElMessage.success('已更新')
    fulfillmentVisible.value = false
    loadRecords()
  } catch (e) { ElMessage.error(e.message || '更新失败') }
}

// --- helpers ---
function typeLabel(t) {
  const m = { COUPON: '优惠券', BALANCE: '余额', THANKS: '谢谢参与', PHYSICAL: '实物' }
  return m[t] || t
}
function typeTag(t) {
  const m = { COUPON: 'success', BALANCE: 'danger', THANKS: 'info', PHYSICAL: 'warning' }
  return m[t] || 'info'
}
function fulfillmentLabel(s) {
  const m = { 0: '待发货', 1: '已发货', 2: '已收货' }
  return m[s] ?? '-'
}
function fulfillmentTag(s) {
  const m = { 0: 'warning', 1: 'primary', 2: 'success' }
  return m[s] ?? 'info'
}

onMounted(() => { loadConfig(); loadPrizes(); loadRecords() })
</script>

<style scoped>
.lottery-admin { padding: 0 0 24px; }
.page-heading { font-size: 20px; font-weight: 700; margin-bottom: 20px; }
.tab-body { padding-top: 8px; }
.toolbar { margin-bottom: 16px; }
.config-form { max-width: 460px; }
</style>
