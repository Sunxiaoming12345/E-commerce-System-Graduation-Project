import request from '@/utils/request'

export function getOrdersPage(params) {
  return request({ url: '/admin/orders/page', method: 'get', params })
}

export function getOrderDetail(id) {
  return request({ url: `/admin/orders/detail/${id}`, method: 'get' })
}

export function updateOrderStatus(data) {
  return request({ url: '/admin/orders/updateStatus', method: 'put', data })
}

export function getOrderStatistics() {
  return request({ url: '/admin/orders/statistics', method: 'get' })
}
