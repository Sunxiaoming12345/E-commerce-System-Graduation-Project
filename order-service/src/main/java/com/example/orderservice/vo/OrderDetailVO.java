package com.example.orderservice.vo;

import com.example.orderservice.entity.Orders;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单详情VO
 *
 * @author sunxiaoming
 * @date 2026-02-28
 */
@Data
public class OrderDetailVO {
    /**
     * 订单基本信息
     */
    private Orders order;

    /**
     * 订单项列表
     */
    private List<OrderItemVO> orderItems;

    /**
     * 支付剩余时间（秒）
     */
    private Integer paymentRemainingTime;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券类型（0=满减 1=折扣）
     */
    private Integer couponType;

    /**
     * 优惠券抵扣金额
     */
    private BigDecimal couponDiscount;

    /**
     * 优惠券最低消费金额
     */
    private BigDecimal couponMinAmount;
}
