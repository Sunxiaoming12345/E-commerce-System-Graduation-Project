package com.example.orderservice.service;

import com.example.orderservice.vo.CategoryVO;
import com.example.orderservice.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author sunxiaoming
 * @date 2026-02-20
 */
public interface ProductService {
    /**
     * 获取推荐商品列表
     *
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendedProducts();

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<CategoryVO> getAllCategories();

    /**
     * 根据分类ID获取商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<ProductVO> getProductsByCategoryId(Long categoryId);

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getProductById(Long id);

    /**
     * 清除推荐商品缓存
     */
    void clearRecommendedProductsCache();

    /**
     * 清除分类商品缓存
     * @param categoryId 分类ID
     */
    void clearCategoryProductsCache(Long categoryId);

    /**
     * 清除所有分类商品缓存
     */
    void clearAllCategoryProductsCache();

    /**
     * 清除分类列表缓存
     */
    void clearCategoriesCache();

    /**
     * 搜索商品
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    List<ProductVO> searchProducts(String keyword);
}
