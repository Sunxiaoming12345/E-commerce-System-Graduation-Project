package com.example.mailuser.mapper;

import com.example.mailuser.vo.CategoryVO;
import com.example.mailuser.vo.ProductVO;
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
    List<ProductVO> getProductsByCategoryId(@Param("categoryId") Integer categoryId);
}
