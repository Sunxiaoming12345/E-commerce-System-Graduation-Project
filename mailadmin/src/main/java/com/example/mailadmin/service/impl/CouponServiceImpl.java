package com.example.mailadmin.service.impl;
import com.example.exception.BusinessException;

import com.alibaba.fastjson.JSONObject;
import com.example.constant.RabbitMQConstant;
import com.example.entity.Coupon;
import com.example.mailadmin.dto.AddCouponDTO;
import com.example.mailadmin.dto.CouponPageQueryDTO;
import com.example.mailadmin.dto.EditCouponDTO;
import com.example.mailadmin.mapper.CouponMapper;
import com.example.mailadmin.service.CouponService;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    /** 发布优惠券缓存刷新消息，通知 coupon-service 清缓存 */
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
    public PageResult pageQuery(CouponPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Coupon> page = couponMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Coupon getById(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        return coupon;
    }

    @Override
    public void add(AddCouponDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException("优惠券名称不能为空");
        }
        if (dto.getDiscountValue() == null || dto.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("优惠金额必须大于0");
        }
        if (dto.getTotalCount() == null || dto.getTotalCount() <= 0) {
            throw new BusinessException("发放数量必须大于0");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BusinessException("请设置优惠券的开始时间和结束时间");
        }
        LocalDateTime now = LocalDateTime.now();
        // 开始时间不能早于当前，自动使用当前时间
        if (dto.getStartTime().isBefore(now)) {
            dto.setStartTime(now);
        }
        // 结束时间必须大于等于当前时间
        if (dto.getEndTime().isBefore(now)) {
            throw new BusinessException("结束时间不能早于当前时间");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        couponMapper.insert(coupon);
        stringRedisTemplate.delete("coupons:available");
        publishCacheRefresh();
    }

    @Override
    public void edit(EditCouponDTO dto) {
        Coupon existing = couponMapper.selectById(dto.getCouponId());
        if (existing == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException("优惠券名称不能为空");
        }
        if (dto.getDiscountValue() == null || dto.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("优惠金额必须大于0");
        }
        if (dto.getTotalCount() == null || dto.getTotalCount() <= 0) {
            throw new BusinessException("发放数量必须大于0");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BusinessException("请设置优惠券的开始时间和结束时间");
        }
        LocalDateTime now = LocalDateTime.now();
        // 仅当开始时间被修改且早于当前时间时才拒绝，已生效的优惠券允许保留原开始时间
        if (dto.getStartTime().isBefore(now) && !dto.getStartTime().equals(existing.getStartTime())) {
            throw new BusinessException("开始时间不能早于当前时间");
        }
        if (dto.getEndTime().isBefore(now)) {
            throw new BusinessException("结束时间不能早于当前时间");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        couponMapper.update(coupon);
        stringRedisTemplate.delete("coupons:available");
        publishCacheRefresh();
    }

    @Override
    public void delete(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        couponMapper.deleteById(id);
        stringRedisTemplate.delete("coupons:available");
        publishCacheRefresh();
    }
}
