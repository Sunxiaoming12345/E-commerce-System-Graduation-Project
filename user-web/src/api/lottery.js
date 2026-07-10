import request from '@/utils/request'

export function getPrizes() {
  return request({ url: '/user/lottery/prizes', method: 'get' })
}

export function getSpinCost() {
  return request({ url: '/user/lottery/cost', method: 'get' })
}

export function checkSpinStatus() {
  return request({ url: '/user/lottery/status', method: 'get' })
}

export function doSpin() {
  return request({ url: '/user/lottery/spin', method: 'post' })
}

export function getMyRecords(page = 1, pageSize = 10) {
  return request({ url: '/user/lottery/records', method: 'get', params: { page, pageSize } })
}
