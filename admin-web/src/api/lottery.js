import request from '@/utils/request'

// 配置
export function getConfig() { return request({ url: '/admin/lottery/config', method: 'get' }) }
export function updateConfig(data) { return request({ url: '/admin/lottery/config', method: 'put', data }) }

// 奖池
export function getPrizes() { return request({ url: '/admin/lottery/prizes', method: 'get' }) }
export function getPrizeById(id) { return request({ url: `/admin/lottery/prizes/${id}`, method: 'get' }) }
export function addPrize(data) { return request({ url: '/admin/lottery/prizes', method: 'post', data }) }
export function editPrize(data) { return request({ url: '/admin/lottery/prizes', method: 'put', data }) }
export function deletePrize(id) { return request({ url: `/admin/lottery/prizes/${id}`, method: 'delete' }) }

// 记录
export function getRecords(params) { return request({ url: '/admin/lottery/records', method: 'get', params }) }
export function getRecordById(id) { return request({ url: `/admin/lottery/records/${id}`, method: 'get' }) }
export function updateFulfillment(data) { return request({ url: '/admin/lottery/records/fulfillment', method: 'put', data }) }
