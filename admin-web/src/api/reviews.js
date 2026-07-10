import request from '@/utils/request'

export function getReviewsPage(params) {
  return request({ url: '/admin/reviews/page', method: 'get', params })
}

export function updateReviewStatus(data) {
  return request({ url: '/admin/reviews/updateStatus', method: 'put', data })
}

export function deleteReview(id) {
  return request({ url: `/admin/reviews/${id}`, method: 'delete' })
}
