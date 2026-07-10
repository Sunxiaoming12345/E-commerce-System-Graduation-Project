package com.example.cartservice.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车VO
 *
 * @author sunxiaoming
 * @date 2026-02-10
 */
@Data
public class CartVO {
    /**
     * 购物车ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String imageUrl;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品库存
     */
    private Integer stock;

    /**
     * 商品状态
     */
    private Integer status;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
