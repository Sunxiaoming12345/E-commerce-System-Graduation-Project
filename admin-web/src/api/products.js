import request from '@/utils/request'

export function getProductsPage(params) {
  return request({ url: '/products/products/page', method: 'get', params })
}

export function getProductById(id) {
  return request({ url: `/products/products/${id}`, method: 'get' })
}

export function addProduct(data) {
  return request({ url: '/products/products/add', method: 'post', data })
}

export function editProduct(data) {
  return request({ url: '/products/products/edit', method: 'put', data })
}

export function removeProducts(ids) {
  return request({ url: `/products/products/remove/${ids.join(',')}`, method: 'delete' })
}

export function updateStock(id, stock) {
  return request({ url: '/products/products/updateStock', method: 'put', params: { id, stock } })
}

export function batchUpdateStock(data) {
  return request({ url: '/products/products/batchUpdateStock', method: 'put', data })
}

export function enableProducts(ids) {
  return request({ url: `/products/products/enable/${ids.join(',')}`, method: 'put' })
}

export function disableProducts(ids) {
  return request({ url: `/products/products/disable/${ids.join(',')}`, method: 'put' })
}
