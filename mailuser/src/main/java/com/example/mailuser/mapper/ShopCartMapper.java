package com.example.mailuser.mapper;

import com.example.mailuser.vo.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车Mapper
 *
 * @author sunxiaoming
 * @date 2026-02-10
 */
@Mapper
public interface ShopCartMapper {

    /**
     * 添加商品到购物车
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @param quantity  商品数量
     * @return 影响行数
     */
    int addCart(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 更新购物车商品数量
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @param quantity  商品数量
     * @return 影响行数
     */
    int updateQuantity(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 删除购物车商品
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @return 影响行数
     */
    int deleteCart(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int clearCart(@Param("userId") Long userId);

    /**
     * 查询用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartVO> getCartList(@Param("userId") Long userId);

    /**
     * 检查商品是否在购物车中
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @return 购物车记录数
     */
    int checkCartItem(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 获取购物车中商品的数量
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @return 商品数量
     */
    Integer getQuantity(@Param("userId") Long userId, @Param("productId") Long productId);

}
