import request from '@/utils/request'

export function createOrder(data) {
    return request({
        url: '/user/orders/create',
        method: 'post',
        data
    })
}

export function getOrders(params) {
  return request({
    url: '/user/orders/myOrders',
    method: 'get',
    params
  })
}

export function getOrderDetail(id) {
    return request({
        url: `/user/orders/${id}`,
        method: 'get'
    })
}

export function cancelOrder(id) {
    return request({
        url: `/user/orders/cancel/${id}`,
        method: 'put'
    })
}

export function payOrder(data) {
    return request({
        url: '/user/orders/pay',
        method: 'post',
        data
    })
}

export function prepurchase(data) {
    return request({
        url: '/user/orders/prepurchase',
        method: 'post',
        data
    })
}