package com.example.mailuser.service.impl;

import com.example.mailuser.dto.AddCartDTO;
import com.example.mailuser.mapper.ProductMapper;
import com.example.mailuser.mapper.ShopCartMapper;
import com.example.mailuser.service.ShopCartService;
import com.example.mailuser.vo.CartVO;
import com.example.mailuser.vo.ProductVO;
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

    @Autowired
    private ProductMapper productMapper;

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

        // 检查商品是否存在
        ProductVO product = productMapper.getProductById(productId);
        if (product == null) {
            log.error("商品不存在：productId={}", productId);
            throw new RuntimeException("商品不存在");
        }

        // 检查商品库存
        if (product.getStock() < quantity) {
            log.error("商品库存不足：productId={}, 库存={}, 需求={}", productId, product.getStock(), quantity);
            throw new RuntimeException("商品库存不足");
        }

        // 检查商品是否已经在购物车中
        int count = shopCartMapper.checkCartItem(userId, productId);

        if (count > 0) {
            // 商品已在购物车中，获取原有数量并相加
            Integer existingQuantity = shopCartMapper.getQuantity(userId, productId);
            Integer newQuantity = existingQuantity + quantity;
            // 检查库存是否足够
            if (product.getStock() < newQuantity) {
                log.error("商品库存不足：productId={}, 库存={}, 需求={}", productId, product.getStock(), newQuantity);
                throw new RuntimeException("商品库存不足");
            }
            // 更新数量
            shopCartMapper.updateQuantity(userId, productId, newQuantity);
            log.info("更新购物车商品数量：userId={}, productId={}, quantity={}", userId, productId, newQuantity);
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
