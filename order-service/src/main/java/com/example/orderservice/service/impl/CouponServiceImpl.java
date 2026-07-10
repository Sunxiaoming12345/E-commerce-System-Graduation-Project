package com.example.orderservice.service.impl;
import com.example.exception.BusinessException;

import com.alibaba.fastjson.JSONObject;
import com.example.constant.RabbitMQConstant;
import com.example.entity.Coupon;
import com.example.entity.UserCoupon;
import com.example.orderservice.mapper.CouponMapper;
import com.example.orderservice.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String MY_COUPONS_PREFIX = "user:coupons:";

    private void deleteMyCouponsCache(Integer userId) {
        for (int s = 0; s <= 2; s++) {
            stringRedisTemplate.delete(MY_COUPONS_PREFIX + userId + ":" + s);
        }
        stringRedisTemplate.delete(MY_COUPONS_PREFIX + userId + ":all");
    }

    private void publishCacheRefresh() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("timestamp", System.currentTimeMillis());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.COUPON_CACHE_EXCHANGE_NAME,
                    RabbitMQConstant.COUPON_CACHE_ROUTING_KEY,
                    msg.toJSONString());
        } catch (Exception e) {
            log.warn("发布优惠券缓存刷新消息失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal useCoupon(Long userCouponId, Long orderId, Integer userId, BigDecimal orderAmount) {
        // 1. 查询用户优惠券记录
        UserCoupon uc = couponMapper.selectUserCouponById(userCouponId);
        if (uc == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (uc.getStatus() != 0) {
            throw new BusinessException("优惠券已使用或已过期");
        }
        if (!uc.getUserId().equals(userId)) {
            throw new BusinessException("优惠券不属于当前用户");
        }

        // 2. 查询优惠券模板
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券已失效");
        }
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }

        // 3. 校验最低消费金额
        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("未达到最低消费金额 ¥" + coupon.getMinAmount());
        }

        // 4. 计算折扣金额
        BigDecimal discount;
        if (coupon.getType() == 0) {
            discount = coupon.getDiscountValue();
        } else {
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue()));
            discount = discount.setScale(2, RoundingMode.DOWN);
        }

        // 5. 原子更新用户优惠券状态为已使用（WHERE status=0 防止重复使用）
        int rows = couponMapper.updateUserCouponStatus(userCouponId, 1, orderId);
        if (rows == 0) {
            throw new BusinessException("优惠券已被使用");
        }

        log.info("优惠券使用成功：userId={}, userCouponId={}, orderId={}, discount={}", userId, userCouponId, orderId, discount);
        // 失效用户优惠券缓存
        deleteMyCouponsCache(userId);
        return discount;
    }

    @Override
    public void releaseCoupon(Long orderId) {
        int rows = couponMapper.releaseUserCoupon(orderId);
        if (rows > 0) {
            log.info("优惠券已释放：orderId={}", orderId);
        }
    }
}
