package com.example.mailuser.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建DTO
 *
 * @author sunxiaoming
 * @date 2026-02-27
 */
@Data
public class OrderCreateDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单号
     */
    private String orderNumber;

    /**
     * 订单商品列表
     */
    private List<OrderItemDTO> items;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 支付方式
     */
    private Integer paymentMethod;

    /**
     * 幂等令牌，防止重复提交
     */
    private String idempotentToken;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 用户优惠券ID（可选）
     */
    private Long userCouponId;

    /**
     * 订单商品项DTO
     */
    @Data
    public static class OrderItemDTO {
        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品数量
         */
        private Integer quantity;
    }
}
