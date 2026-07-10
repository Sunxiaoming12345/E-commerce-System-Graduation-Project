package com.example.cartservice.service.impl;
import com.example.exception.BusinessException;

import com.example.cartservice.dto.AddCartDTO;
import com.example.cartservice.mapper.ProductMapper;
import com.example.cartservice.mapper.ShopCartMapper;
import com.example.cartservice.service.ShopCartService;
import com.example.cartservice.vo.CartVO;
import com.example.cartservice.vo.ProductVO;
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
            throw new BusinessException("商品不存在");
        }

        // 检查商品是否已下架
        if (product.getStatus() == 0) {
            log.error("商品已下架：productId={}", productId);
            throw new BusinessException("商品已下架");
        }

        // 检查商品库存
        if (product.getStock() < quantity) {
            log.error("商品库存不足：productId={}, 库存={}, 需求={}", productId, product.getStock(), quantity);
            throw new BusinessException("商品库存不足");
        }

        // 使用 INSERT + catch DuplicateKeyException + UPDATE 替代 ON DUPLICATE KEY UPDATE
        // 因为 ShardingSphere 的 SQL 解析器会丢弃 ON DUPLICATE KEY UPDATE 子句
        try {
            shopCartMapper.insertCart(userId, productId, quantity);
            log.info("新增购物车商品：userId={}, productId={}, quantity={}", userId, productId, quantity);
        } catch (Exception e) {
            // 重复键 → 累加数量（原子 UPDATE，避免并发问题）
            shopCartMapper.incrementQuantity(userId, productId, quantity);
            log.info("累加购物车商品数量：userId={}, productId={}, quantity={}", userId, productId, quantity);
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
