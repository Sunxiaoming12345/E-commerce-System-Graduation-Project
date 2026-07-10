<template>
  <div class="reviews-page">
    <div class="toolbar">
      <el-input v-model="query.productId" placeholder="商品ID" clearable style="width:150px" @change="loadList" />
      <el-select v-model="query.rating" placeholder="评分" clearable style="width:120px" @change="loadList">
        <el-option label="1星" :value="1" />
        <el-option label="2星" :value="2" />
        <el-option label="3星" :value="3" />
        <el-option label="4星" :value="4" />
        <el-option label="5星" :value="5" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="loadList">
        <el-option label="显示" :value="1" />
        <el-option label="隐藏" :value="0" />
      </el-select>
    </div>

    <el-table :data="list" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="reviewId" label="ID" width="80" />
      <el-table-column prop="productId" label="商品ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column label="评分" width="120">
        <template #default="{ row }">
          <span style="color:#f59e0b">{{ '★'.repeat(row.rating) }}{{ '☆'.repeat(5 - row.rating) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleToggle(row)">
            {{ row.status === 1 ? '隐藏' : '显示' }}
          </el-button>
          <el-button type="danger" link @click="handleRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList" @size-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewsPage, updateReviewStatus, deleteReview } from '@/api/reviews'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive({ productId: null, rating: null, status: null, page: 1, pageSize: 10 })

async function loadList() {
  loading.value = true
  try {
    const res = await getReviewsPage(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function handleToggle(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateReviewStatus({ reviewId: row.reviewId, status: newStatus })
  ElMessage.success(newStatus === 1 ? '已显示' : '已隐藏')
  loadList()
}

async function handleRemove(row) {
  await ElMessageBox.confirm('确定删除该评价吗？', '提示', { type: 'warning' })
  await deleteReview(row.reviewId)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.reviews-page { animation: fade-in 0.25s var(--ease); }
@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }

.toolbar { margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap; }

/* === Table === */
.reviews-page :deep(.el-table) {
  --el-table-bg-color: #fff; --el-table-tr-bg-color: #fff;
  --el-table-header-bg-color: #fafaf9; --el-table-row-hover-bg-color: #f5f3f0;
  --el-table-border-color: #111; --el-table-text-color: #111;
  --el-table-header-text-color: #111;
  border: 3px solid #111; box-shadow: var(--shadow-hard);
}

.reviews-page :deep(.el-table th.el-table__cell) {
  background: #111; color: #fff;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em;
  border-bottom: 2px solid #111;
}
.reviews-page :deep(.el-table td.el-table__cell) { border-bottom: 2px solid #e5e3e0; }

.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.reviews-page :deep(.el-pager li) { border: 2px solid #111 !important; font-weight: 700 !important; }
.reviews-page :deep(.el-pager li.is-active) { background: #111 !important; color: #fff !important; }
</style>
