package com.example.mailrefund.service.impl;
import com.alibaba.fastjson.JSON;
import com.example.exception.BusinessException;

import com.example.constant.OrderStatus;
import com.example.entity.Refund;
import com.example.mailrefund.dto.RefundSubmitDTO;
import com.example.mailrefund.mapper.RefundMapper;
import com.example.mailrefund.service.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RefundServiceImpl implements RefundService {

    private static final String REFUNDS_CACHE_PREFIX = "user:refunds:";
    private static final long REFUNDS_CACHE_TTL = 5;
    private static final TimeUnit REFUNDS_CACHE_TTL_UNIT = TimeUnit.MINUTES;

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void submitRefund(RefundSubmitDTO dto, Integer userId) {
        if (dto.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount().doubleValue() <= 0) {
            throw new BusinessException("退款金额必须大于0");
        }
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new BusinessException("退款原因不能为空");
        }
        Map<String, Object> order = jdbcTemplate.queryForMap(
                "SELECT order_status, user_id FROM orders WHERE order_id = ?", dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        int orderStatus = (int) order.get("order_status");
        int orderUserId = (int) order.get("user_id");
        if (orderUserId != userId) {
            throw new BusinessException("无权操作此订单");
        }
        if (orderStatus != OrderStatus.PAID && orderStatus != OrderStatus.DELIVERED) {
            throw new BusinessException("当前订单状态不可退款");
        }
        int pendingCount = refundMapper.countPendingByOrderId(dto.getOrderId());
        if (pendingCount > 0) {
            throw new BusinessException("已有待处理的退款申请");
        }
        Refund refund = new Refund();
        refund.setOrderId(dto.getOrderId());
        refund.setUserId(userId);
        refund.setAmount(dto.getAmount());
        refund.setReason(dto.getReason());
        refundMapper.insert(refund);
        jdbcTemplate.update("UPDATE orders SET order_status = ?, update_time = NOW() WHERE order_id = ?",
                OrderStatus.REFUNDING, dto.getOrderId());
        // 清除退款列表缓存 + 订单列表缓存
        stringRedisTemplate.delete(REFUNDS_CACHE_PREFIX + userId);
        java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + userId + ":*");
        if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        log.info("退款申请已提交，订单状态已更新为退款中：orderId={}", dto.getOrderId());
    }

    @Override
    public List<Refund> getMyRefunds(Integer userId) {
        // 1. 尝试从 Redis 缓存读取
        String key = REFUNDS_CACHE_PREFIX + userId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSON.parseArray(cached, Refund.class);
        }
        // 2. 缓存未命中，查 DB
        List<Refund> list = refundMapper.selectByUserId(userId);
        // 3. 写入缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(list),
                REFUNDS_CACHE_TTL, REFUNDS_CACHE_TTL_UNIT);
        return list;
    }

    @Override
    public Refund getById(Long refundId) {
        return refundMapper.selectById(refundId);
    }
}
