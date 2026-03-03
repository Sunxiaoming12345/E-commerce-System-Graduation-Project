package com.example.mailuser.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.mailuser.mapper.ProductMapper;
import com.example.mailuser.service.ProductService;
import com.example.mailuser.vo.CategoryVO;
import com.example.mailuser.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String CATEGORIES_CACHE_KEY = "categories:all";
    private static final String RECOMMENDED_PRODUCTS_CACHE_KEY = "products:recommended";
    private static final long CACHE_EXPIRE_TIME = 3600; // 缓存过期时间，单位：秒

    /**
     * 获取推荐商品列表
     *
     * @return 推荐商品列表
     */
    @Override
    public List<ProductVO> getRecommendedProducts() {
        // 尝试从Redis缓存中获取推荐商品数据
        try {
            String productsJson = redisTemplate.opsForValue().get(RECOMMENDED_PRODUCTS_CACHE_KEY);
            if (productsJson != null) {
                log.info("从Redis缓存中获取推荐商品数据");
                return JSON.parseArray(productsJson, ProductVO.class);
            }
        } catch (Exception e) {
            log.error("Redis缓存读取失败：{}", e.getMessage());
        }

        // 从数据库中获取推荐商品数据
        List<ProductVO> recommendedProducts = productMapper.getRecommendedProducts();
        log.info("从数据库中获取推荐商品列表，数量：{}", recommendedProducts.size());

        // 将推荐商品数据存入Redis缓存
        try {
            String productsJson = JSON.toJSONString(recommendedProducts);
            redisTemplate.opsForValue().set(RECOMMENDED_PRODUCTS_CACHE_KEY, productsJson, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.info("推荐商品数据已存入Redis缓存，过期时间：{}秒", CACHE_EXPIRE_TIME);
        } catch (Exception e) {
            log.error("Redis缓存写入失败：{}", e.getMessage());
        }

        return recommendedProducts;
    }

    /**
     * 清除推荐商品缓存
     */
    public void clearRecommendedProductsCache() {
        try {
            redisTemplate.delete(RECOMMENDED_PRODUCTS_CACHE_KEY);
            log.info("推荐商品缓存已清除");
        } catch (Exception e) {
            log.error("清除推荐商品缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    @Override
    public List<CategoryVO> getAllCategories() {
        // 尝试从Redis缓存中获取分类数据
        try {
            String categoriesJson = redisTemplate.opsForValue().get(CATEGORIES_CACHE_KEY);
            if (categoriesJson != null) {
                log.info("从Redis缓存中获取分类数据");
                return JSON.parseArray(categoriesJson, CategoryVO.class);
            }
        } catch (Exception e) {
            log.error("Redis缓存读取失败：{}", e.getMessage());
        }

        // 从数据库中获取分类数据
        List<CategoryVO> categories = productMapper.getAllCategories();
        log.info("从数据库中获取所有分类，数量：{}", categories.size());

        // 将分类数据存入Redis缓存
        try {
            String categoriesJson = JSON.toJSONString(categories);
            redisTemplate.opsForValue().set(CATEGORIES_CACHE_KEY, categoriesJson, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.info("分类数据已存入Redis缓存，过期时间：{}秒", CACHE_EXPIRE_TIME);
        } catch (Exception e) {
            log.error("Redis缓存写入失败：{}", e.getMessage());
        }

        return categories;
    }

    /**
     * 根据分类ID获取商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @Override
    public List<ProductVO> getProductsByCategoryId(Long categoryId) {
        // 生成分类商品缓存键
        String categoryProductsCacheKey = "products:category:" + categoryId;
        
        // 尝试从Redis缓存中获取分类商品数据
        try {
            String productsJson = redisTemplate.opsForValue().get(categoryProductsCacheKey);
            if (productsJson != null) {
                log.info("从Redis缓存中获取分类商品数据，分类ID：{}", categoryId);
                return JSON.parseArray(productsJson, ProductVO.class);
            }
        } catch (Exception e) {
            log.error("Redis缓存读取失败：{}", e.getMessage());
        }

        // 从数据库中获取分类商品数据
        List<ProductVO> products = productMapper.getProductsByCategoryId(categoryId);
        log.info("从数据库中获取分类商品列表，分类ID：{}，商品数量：{}", categoryId, products.size());

        // 将分类商品数据存入Redis缓存
        try {
            String productsJson = JSON.toJSONString(products);
            redisTemplate.opsForValue().set(categoryProductsCacheKey, productsJson, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.info("分类商品数据已存入Redis缓存，分类ID：{}，过期时间：{}秒", categoryId, CACHE_EXPIRE_TIME);
        } catch (Exception e) {
            log.error("Redis缓存写入失败：{}", e.getMessage());
        }

        return products;
    }

    /**
     * 清除分类商品缓存
     * @param categoryId 分类ID
     */
    public void clearCategoryProductsCache(Long categoryId) {
        try {
            String categoryProductsCacheKey = "products:category:" + categoryId;
            redisTemplate.delete(categoryProductsCacheKey);
            log.info("分类商品缓存已清除，分类ID：{}", categoryId);
        } catch (Exception e) {
            log.error("清除分类商品缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 清除所有分类商品缓存
     */
    public void clearAllCategoryProductsCache() {
        try {
            // 使用通配符删除所有分类商品缓存
            redisTemplate.delete(redisTemplate.keys("products:category:*"));
            log.info("所有分类商品缓存已清除");
        } catch (Exception e) {
            log.error("清除所有分类商品缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 清除分类列表缓存
     */
    public void clearCategoriesCache() {
        try {
            redisTemplate.delete(CATEGORIES_CACHE_KEY);
            log.info("分类列表缓存已清除");
        } catch (Exception e) {
            log.error("清除分类列表缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @Override
    public ProductVO getProductById(Long id) {
        ProductVO product = productMapper.getProductById(id);
        log.info("根据ID获取商品详情，商品ID：{}，商品：{}", id, product);
        return product;
    }
}
