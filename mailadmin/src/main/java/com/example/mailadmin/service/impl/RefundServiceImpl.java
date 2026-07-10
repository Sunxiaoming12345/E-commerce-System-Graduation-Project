package com.example.mailadmin.service.impl;
import com.example.exception.BusinessException;

import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.entity.Refund;
import com.example.mailadmin.dto.RefundApproveDTO;
import com.example.mailadmin.dto.RefundPageQueryDTO;
import com.example.mailadmin.mapper.RefundMapper;
import com.example.mailadmin.service.RefundService;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult pageQuery(RefundPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Refund> page = refundMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Refund getById(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        return refund;
    }

    @Override
    public void approve(RefundApproveDTO dto) {
        Refund refund = refundMapper.selectById(dto.getRefundId());
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException("该退款已处理");
        }
        // 清除该用户的退款列表缓存 + 订单列表缓存
        stringRedisTemplate.delete("user:refunds:" + refund.getUserId());
        java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + refund.getUserId() + ":*");
        if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);

        if (dto.getApproved() != null && dto.getApproved()) {
            // 通过：更新退款状态为已通过
            refundMapper.updateStatus(dto.getRefundId(), 1, dto.getAdminRemark());
            // 发送退款消息（异步退库存/退余额），由 consumer 完成后再改订单状态
            rabbitTemplate.convertAndSend(RabbitMQConstant.REFUND_EXCHANGE_NAME,
                    RabbitMQConstant.REFUND_ROUTING_KEY, refund);
            log.info("退款审批通过，已发送退款消息：refundId={}", refund.getRefundId());
        } else {
            // 拒绝：更新退款状态，恢复订单状态为已付款
            refundMapper.updateStatus(dto.getRefundId(), 2, dto.getAdminRemark());
            jdbcTemplate.update("UPDATE orders SET order_status = ?, update_time = NOW() WHERE order_id = ?",
                    OrderStatus.PAID, refund.getOrderId());
        }
    }
}
