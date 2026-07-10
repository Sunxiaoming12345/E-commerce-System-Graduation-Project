import request from '@/utils/request'

export function getCouponsPage(params) {
  return request({ url: '/admin/coupons/page', method: 'get', params })
}

export function getCouponById(id) {
  return request({ url: `/admin/coupons/${id}`, method: 'get' })
}

export function addCoupon(data) {
  return request({ url: '/admin/coupons/add', method: 'post', data })
}

export function editCoupon(data) {
  return request({ url: '/admin/coupons/edit', method: 'put', data })
}

export function deleteCoupon(id) {
  return request({ url: `/admin/coupons/${id}`, method: 'delete' })
}
