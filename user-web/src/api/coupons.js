import request from '@/utils/request'

export function getAvailableCoupons() {
  return request({ url: '/user/coupons/available', method: 'get' })
}

export function claimCoupon(couponId) {
  return request({ url: `/user/coupons/claim/${couponId}`, method: 'post' })
}

export function getMyCoupons(status) {
  return request({ url: '/user/coupons/my', method: 'get', params: status != null ? { status } : {} })
}

export function previewCouponDiscount(userCouponId, orderAmount) {
  return request({ url: '/user/coupons/preview', method: 'post', data: { userCouponId, orderAmount } })
}
