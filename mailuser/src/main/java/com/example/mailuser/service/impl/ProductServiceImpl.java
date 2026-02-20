package com.example.mailuser.service.impl;

import com.example.mailuser.mapper.ProductMapper;
import com.example.mailuser.service.ProductService;
import com.example.mailuser.vo.CategoryVO;
import com.example.mailuser.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现类
 *
 * @author sunxiaoming
 * @date 2026-02-20
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取推荐商品列表
     *
     * @return 推荐商品列表
     */
    @Override
    public List<ProductVO> getRecommendedProducts() {
        List<ProductVO> recommendedProducts = productMapper.getRecommendedProducts();
        log.info("获取推荐商品列表，数量：{}", recommendedProducts.size());
        return recommendedProducts;
    }

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    @Override
    public List<CategoryVO> getAllCategories() {
        List<CategoryVO> categories = productMapper.getAllCategories();
        log.info("获取所有分类，数量：{}", categories.size());
        return categories;
    }

    /**
     * 根据分类ID获取商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @Override
    public List<ProductVO> getProductsByCategoryId(Integer categoryId) {
        List<ProductVO> products = productMapper.getProductsByCategoryId(categoryId);
        log.info("根据分类ID获取商品列表，分类ID：{}，商品数量：{}", categoryId, products.size());
        return products;
    }
}
