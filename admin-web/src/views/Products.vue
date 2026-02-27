<template>
  <div class="products-page">
    <div class="toolbar">
      <el-input v-model="query.name" placeholder="商品名称" clearable style="width: 180px" @clear="onSearch" />
      <el-select v-model="query.categoryId" placeholder="分类" clearable style="width: 140px" @change="onSearch">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 100px" @change="onSearch">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
      <el-button @click="openAdd">新增商品</el-button>
    </div>
    <el-table :data="list" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ getCategoryName(row.categoryId ?? row.category_id) }}</template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥ {{ formatMoney(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '上架' : '下架' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="primary" link @click="openStock(row)">库存</el-button>
          <el-button v-if="row.status === 0" type="success" link @click="enable([row.id])">上架</el-button>
          <el-button v-else type="warning" link @click="disable([row.id])">下架</el-button>
          <el-button type="danger" link @click="handleRemove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="主图" prop="imageUrl">
          <el-upload
            class="avatar-uploader"
            :http-request="uploadImage"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <el-input v-model="form.imageUrl" placeholder="图片 URL" style="margin-top: 10px" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="商品描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialogVisible" title="修改库存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前库存">{{ currentProduct?.stock }}</el-form-item>
        <el-form-item label="新库存">
          <el-input-number v-model="stockForm.stock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getProductsPage, getProductById, addProduct, editProduct, removeProducts, updateStock, enableProducts, disableProducts } from '@/api/products'
import { getCategoryList } from '@/api/categories'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const query = reactive({ name: '', categoryId: null, status: null, page: 1, pageSize: 10 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, name: '', description: '', imageUrl: '', categoryId: null, price: 0, stock: 0, status: 1
})
const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const stockDialogVisible = ref(false)
const currentProduct = ref(null)
const stockForm = reactive({ stock: 0 })

function formatMoney(v) {
  if (v == null) return '0.00'
  return Number(v).toFixed(2)
}

function getCategoryName(categoryId) {
  if (categoryId == null) return '-'
  const c = categories.value.find((x) => (x.id || x.categoryId) == categoryId)
  return c ? c.name : '-'
}

function onSearch() {
  query.page = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res = await getProductsPage({
      name: query.name || undefined,
      categoryId: query.categoryId ?? undefined,
      status: query.status ?? undefined,
      page: query.page,
      pageSize: query.pageSize
    })
    list.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categories.value = await getCategoryList() || []
  } catch (e) {
    categories.value = []
  }
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', description: '', imageUrl: '', categoryId: null, price: 0, stock: 0, status: 1 })
  dialogVisible.value = true
}

async function openEdit(row) {
  isEdit.value = true
  try {
    const detail = await getProductById(row.id)
    Object.assign(form, { ...detail, categoryId: detail.categoryId ?? detail.category_id })
  } catch (e) {
    Object.assign(form, { ...row, categoryId: row.categoryId ?? row.category_id })
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  if (isEdit.value) {
    await editProduct({ id: form.id, name: form.name, description: form.description, imageUrl: form.imageUrl, categoryId: form.categoryId, price: form.price, stock: form.stock, status: form.status })
    ElMessage.success('修改成功')
  } else {
    await addProduct({ name: form.name, description: form.description, imageUrl: form.imageUrl, categoryId: form.categoryId, price: form.price, stock: form.stock, status: form.status })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadList()
}

function openStock(row) {
  currentProduct.value = row
  stockForm.stock = row.stock
  stockDialogVisible.value = true
}

async function submitStock() {
  await updateStock(currentProduct.value.id, stockForm.stock)
  ElMessage.success('库存已更新')
  stockDialogVisible.value = false
  loadList()
}

async function enable(ids) {
  await enableProducts(ids)
  ElMessage.success('已上架')
  loadList()
}

async function disable(ids) {
  await disableProducts(ids)
  ElMessage.success('已下架')
  loadList()
}

async function handleRemove(row) {
  await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
  await removeProducts([row.id])
  ElMessage.success('删除成功')
  loadList()
}

async function uploadImage(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  
  try {
    const response = await request.post('/products/products/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    form.imageUrl = response
    ElMessage.success('上传成功')
    options.onSuccess(response)
  } catch (error) {
    ElMessage.error('上传失败')
    options.onError(error)
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

onMounted(() => {
  loadCategories()
  loadList()
})
</script>

<style scoped>
.products-page .toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.products-page .pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
}

.avatar-uploader-icon {
  width: 120px;
  height: 120px;
  line-height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  text-align: center;
  color: #999;
  font-size: 24px;
  cursor: pointer;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
