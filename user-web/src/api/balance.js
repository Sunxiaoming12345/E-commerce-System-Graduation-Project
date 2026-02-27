import request from '@/utils/request'

export function getBalance() {
    return request({
        url: '/user/balance/info',
        method: 'get'
    })
}

export function recharge(amount) {
    return request({
        url: '/user/balance/recharge',
        method: 'post',
        params: {
            amount
        }
    })
}

export function checkBalance(amount) {
    return request({
        url: '/user/balance/check',
        method: 'get',
        params: {
            amount
        }
    })
}
