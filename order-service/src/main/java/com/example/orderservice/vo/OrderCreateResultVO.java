package com.example.orderservice.vo;

import lombok.Data;

/**
 * 订单创建结果VO
 * 包含业务订单号与支付超时时间（秒）
 */
@Data
public class OrderCreateResultVO {
    /**
     * 业务订单号（与库表 order_number 一致，异步落库前即可用于支付等）
     */
    private String orderNumber;

    /**
     * 支付超时时间（秒）
     */
    private Integer paymentTimeout;

    public OrderCreateResultVO(String orderNumber, Integer paymentTimeout) {
        this.orderNumber = orderNumber;
        this.paymentTimeout = paymentTimeout;
    }
}
