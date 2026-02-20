package com.example.mailuser.service.impl;

import com.example.mailuser.dto.AddCartDTO;
import com.example.mailuser.mapper.ShopCartMapper;
import com.example.mailuser.service.ShopCartService;
import com.example.mailuser.vo.CartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现类
 *
 * @author sunxiaoming
 * @date 2026-02-10
 */
@Service
@Slf4j
public class ShopCartServiceImpl implements ShopCartService {

    @Autowired
    private ShopCartMapper shopCartMapper;

    /**
     * 添加商品到购物车
     *
     * @param userId     用户ID
     * @param addCartDTO 添加购物车DTO
     */
    @Override
    public void addCart(Long userId, AddCartDTO addCartDTO) {
        Long productId = addCartDTO.getProductId();
        Integer quantity = addCartDTO.getQuantity();

        // 检查商品是否已经在购物车中
        int count = shopCartMapper.checkCartItem(userId, productId);

        if (count > 0) {
            // 商品已在购物车中，更新数量
            shopCartMapper.updateQuantity(userId, productId, quantity);
            log.info("更新购物车商品数量：userId={}, productId={}, quantity={}", userId, productId, quantity);
        } else {
            // 商品不在购物车中，添加新记录
            shopCartMapper.addCart(userId, productId, quantity);
            log.info("添加商品到购物车：userId={}, productId={}, quantity={}", userId, productId, quantity);
        }
    }

    /**
     * 更新购物车商品数量
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @param quantity  商品数量
     */
    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        shopCartMapper.updateQuantity(userId, productId, quantity);
        log.info("更新购物车商品数量：userId={}, productId={}, quantity={}", userId, productId, quantity);
    }

    /**
     * 删除购物车商品
     *
     * @param userId   用户ID
     * @param productId 商品ID
     */
    @Override
    public void deleteCart(Long userId, Long productId) {
        shopCartMapper.deleteCart(userId, productId);
        log.info("删除购物车商品：userId={}, productId={}", userId, productId);
    }

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    @Override
    public void clearCart(Long userId) {
        shopCartMapper.clearCart(userId);
        log.info("清空购物车：userId={}", userId);
    }

    /**
     * 查询用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    @Override
    public List<CartVO> getCartList(Long userId) {
        List<CartVO> cartList = shopCartMapper.getCartList(userId);
        log.info("查询用户购物车列表：userId={}, 商品数量={}", userId, cartList.size());
        return cartList;
    }

}
