<template>
  <div class="categories-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增分类</el-button>
    </div>
    <el-table :data="list" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="分类描述" rows="3" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
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
import { getCategoryList, addCategory, editCategory, removeCategories } from '@/api/categories'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, name: '', description: '', sort: 0 })
const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getCategoryList()
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.description = row.description ?? ''
  form.sort = row.sort ?? 0
  dialogVisible.value = true
}

function resetForm() {
  form.id = null
  form.name = ''
  form.description = ''
  form.sort = 0
  formRef.value?.resetFields()
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  if (isEdit.value) {
    await editCategory({ id: form.id, name: form.name, description: form.description, sort: form.sort })
    ElMessage.success('修改成功')
  } else {
    await addCategory({ name: form.name, description: form.description, sort: form.sort })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadList()
}

async function handleRemove(row) {
  await ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
  await removeCategories([row.id])
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.categories-page .toolbar {
  margin-bottom: 16px;
}
</style>
