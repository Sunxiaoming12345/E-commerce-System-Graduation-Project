package com.example.mailuser.vo;

import lombok.Data;

/**
 * 订单创建结果VO
 * 包含订单ID和支付超时时间（秒）
 */
@Data
public class OrderCreateResultVO {
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 支付超时时间（秒）
     */
    private Integer paymentTimeout;
    
    public OrderCreateResultVO(Long orderId, Integer paymentTimeout) {
        this.orderId = orderId;
        this.paymentTimeout = paymentTimeout;
    }
}
