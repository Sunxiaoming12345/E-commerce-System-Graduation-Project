import request from '@/utils/request'

export function getRefundsPage(params) {
  return request({ url: '/admin/refunds/page', method: 'get', params })
}

export function getRefundDetail(id) {
  return request({ url: `/admin/refunds/${id}`, method: 'get' })
}

export function approveRefund(data) {
  return request({ url: '/admin/refunds/approve', method: 'put', data })
}
