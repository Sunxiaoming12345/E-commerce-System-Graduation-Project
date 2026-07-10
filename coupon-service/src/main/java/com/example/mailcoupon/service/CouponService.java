package com.example.mailcoupon.service;

import com.example.entity.Coupon;
import com.example.mailcoupon.vo.CouponVO;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    List<Coupon> getAvailableCoupons();
    void claimCoupon(Integer userId, Long couponId);
    List<CouponVO> getMyCoupons(Integer userId, Integer status);
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount);
    BigDecimal useCoupon(Long userCouponId, Long orderId, Integer userId, BigDecimal orderAmount);
    void releaseCoupon(Long orderId);
}
