package com.example.orderservice.mapper;

import com.example.orderservice.vo.CategoryVO;
import com.example.orderservice.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper
 *
 * @author sunxiaoming
 * @date 2026-02-20
 */
@Mapper
public interface ProductMapper {
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
    List<ProductVO> getProductsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getProductById(Long id);

    List<ProductVO> getProductsByIds(@Param("ids") List<Long> ids);

    /**
     * 搜索商品
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    List<ProductVO> searchProducts(String keyword);

    /**
     * 扣减库存（原子操作，MySQL 行锁保证并发安全）
     * @return 影响行数，0 表示库存不足
     */
    int updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 恢复库存（原子操作）
     */
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
