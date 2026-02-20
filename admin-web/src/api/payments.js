import request from '@/utils/request'

export function getPaymentsPage(params) {
  return request({ url: '/admin/payments/page', method: 'get', params })
}

export function updatePaymentStatus(paymentId, status) {
  return request({
    url: '/admin/payments/updateStatus',
    method: 'put',
    params: { paymentId, status }
  })
}

export function getPaymentStatistics() {
  return request({ url: '/admin/payments/statistics', method: 'get' })
}
