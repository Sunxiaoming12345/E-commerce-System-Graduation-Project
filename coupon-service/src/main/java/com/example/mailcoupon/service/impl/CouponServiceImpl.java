package com.example.mailcoupon.service.impl;
import com.example.exception.BusinessException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.constant.RabbitMQConstant;
import com.example.entity.Coupon;
import com.example.entity.UserCoupon;
import com.example.mailcoupon.mapper.CouponMapper;
import com.example.mailcoupon.service.CouponService;
import com.example.mailcoupon.vo.CouponVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    private static final String AVAILABLE_KEY = "coupons:available";
    private static final String MY_COUPONS_PREFIX = "user:coupons:";
    private static final long AVAILABLE_TTL = 5;
    private static final long MY_COUPONS_TTL = 5;
    private static final TimeUnit TTL_UNIT = TimeUnit.MINUTES;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /** 发布优惠券缓存刷新消息 */
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
    public List<Coupon> getAvailableCoupons() {
        String cached = stringRedisTemplate.opsForValue().get(AVAILABLE_KEY);
        if (cached != null) {
            return JSON.parseArray(cached, Coupon.class);
        }
        List<Coupon> list = couponMapper.selectAvailable();
        stringRedisTemplate.opsForValue().set(AVAILABLE_KEY, JSON.toJSONString(list), AVAILABLE_TTL, TTL_UNIT);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimCoupon(Integer userId, Long couponId) {
        Coupon coupon = couponMapper.selectAvailable().stream()
                .filter(c -> c.getCouponId().equals(couponId))
                .findFirst()
                .orElse(null);
        if (coupon == null) {
            throw new BusinessException("优惠券不可用或已领完");
        }
        int count = couponMapper.countUserCoupon(userId, couponId);
        if (count > 0) {
            throw new BusinessException("已领取过该优惠券");
        }
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        couponMapper.insertUserCoupon(uc);
        couponMapper.incrementUsedCount(couponId);
        // 失效缓存
        stringRedisTemplate.delete(AVAILABLE_KEY);
        deleteMyCouponsCache(userId);
    }

    @Override
    public List<CouponVO> getMyCoupons(Integer userId, Integer status) {
        String key = MY_COUPONS_PREFIX + userId + ":" + (status != null ? status : "all");
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSON.parseArray(cached, CouponVO.class);
        }
        couponMapper.expireUserCoupons(userId);
        List<CouponVO> list = couponMapper.selectUserCoupons(userId, status);
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(list), MY_COUPONS_TTL, TTL_UNIT);
        return list;
    }

    private void deleteMyCouponsCache(Integer userId) {
        for (int s = 0; s <= 2; s++) {
            stringRedisTemplate.delete(MY_COUPONS_PREFIX + userId + ":" + s);
        }
        stringRedisTemplate.delete(MY_COUPONS_PREFIX + userId + ":all");
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount) {
        UserCoupon uc = couponMapper.selectUserCouponById(userCouponId);
        if (uc == null || uc.getStatus() != 0) {
            throw new BusinessException("优惠券不可用");
        }
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券已失效");
        }
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }
        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("未达到最低消费金额");
        }
        if (coupon.getType() == 0) {
            return coupon.getDiscountValue();
        } else {
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue()));
            return discount.setScale(2, RoundingMode.DOWN);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal useCoupon(Long userCouponId, Long orderId, Integer userId, BigDecimal orderAmount) {
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

        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券已失效");
        }
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }

        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("未达到最低消费金额 ¥" + coupon.getMinAmount());
        }

        BigDecimal discount;
        if (coupon.getType() == 0) {
            discount = coupon.getDiscountValue();
        } else {
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue()));
            discount = discount.setScale(2, RoundingMode.DOWN);
        }

        int rows = couponMapper.updateUserCouponStatus(userCouponId, 1, orderId);
        if (rows == 0) {
            throw new BusinessException("优惠券已被使用");
        }

        log.info("优惠券使用成功：userId={}, userCouponId={}, orderId={}, discount={}", userId, userCouponId, orderId, discount);
        // 失效该用户优惠券缓存
        deleteMyCouponsCache(userId);
        return discount;
    }

    @Override
    public void releaseCoupon(Long orderId) {
        int rows = couponMapper.releaseUserCoupon(orderId);
        if (rows > 0) {
            log.info("优惠券已释放：orderId={}", orderId);
            // 失效该订单对应用户的优惠券缓存
            UserCoupon uc = couponMapper.selectUserCouponByOrderId(orderId);
            if (uc != null) {
                deleteMyCouponsCache(uc.getUserId());
            }
        }
    }
}
