package com.example.orderservice.service;

import java.math.BigDecimal;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 使用优惠券（原子操作）
     *
     * @param userCouponId 用户优惠券ID
     * @param orderId      订单ID
     * @param userId       用户ID
     * @param orderAmount  订单金额
     * @return 优惠金额
     */
    BigDecimal useCoupon(Long userCouponId, Long orderId, Integer userId, BigDecimal orderAmount);

    /**
     * 释放优惠券（订单取消时回退）
     *
     * @param orderId 订单ID
     */
    void releaseCoupon(Long orderId);
}
