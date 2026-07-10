import request from '@/utils/request'

export function submitRefund(data) {
  return request({ url: '/user/refunds/submit', method: 'post', data })
}

export function getMyRefunds() {
  return request({ url: '/user/refunds/my', method: 'get' })
}

export function getRefundDetail(id) {
  return request({ url: `/user/refunds/${id}`, method: 'get' })
}
