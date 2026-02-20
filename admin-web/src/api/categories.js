import request from '@/utils/request'

export function getCategoryList() {
  return request({ url: '/admin/categories/list', method: 'get' })
}

export function getCategoryById(id) {
  return request({ url: `/admin/categories/${id}`, method: 'get' })
}

export function addCategory(data) {
  return request({ url: '/admin/categories/add', method: 'post', data })
}

export function editCategory(data) {
  return request({ url: '/admin/categories/edit', method: 'put', data })
}

export function removeCategories(ids) {
  return request({ url: `/admin/categories/remove/${ids.join(',')}`, method: 'delete' })
}
