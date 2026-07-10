package com.example.cartservice.service;

import com.example.cartservice.dto.AddCartDTO;
import com.example.cartservice.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 *
 * @author sunxiaoming
 * @date 2026-02-10
 */
public interface ShopCartService {

    /**
     * 添加商品到购物车
     *
     * @param userId     用户ID
     * @param addCartDTO 添加购物车DTO
     */
    void addCart(Long userId, AddCartDTO addCartDTO);

    /**
     * 更新购物车商品数量
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @param quantity  商品数量
     */
    void updateQuantity(Long userId, Long productId, Integer quantity);

    /**
     * 删除购物车商品
     *
     * @param userId   用户ID
     * @param productId 商品ID
     */
    void deleteCart(Long userId, Long productId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 查询用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartVO> getCartList(Long userId);

}
