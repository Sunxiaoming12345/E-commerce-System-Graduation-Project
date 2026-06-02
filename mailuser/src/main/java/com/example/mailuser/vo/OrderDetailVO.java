package com.example.mailuser.vo;

import com.example.mailadmin.entity.Payments;
import com.example.mailuser.entity.Orders;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单详情VO
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
     * 支付记录
     */
    private Payments payment;

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
}
