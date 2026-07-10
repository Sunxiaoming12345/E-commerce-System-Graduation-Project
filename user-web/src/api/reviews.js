import request from '@/utils/request'

export function submitReview(data) {
  return request({ url: '/user/reviews/submit', method: 'post', data })
}

export function getProductReviews(productId) {
  return request({ url: `/user/reviews/product/${productId}`, method: 'get' })
}
