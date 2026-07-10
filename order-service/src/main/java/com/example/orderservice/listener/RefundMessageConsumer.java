package com.example.orderservice.listener;

import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.entity.Refund;
import com.example.mailadmin.entity.OrderItems;
import com.example.orderservice.entity.Orders;
import com.example.orderservice.mapper.BalanceMapper;
import com.example.orderservice.mapper.ProductMapper;
import com.example.orderservice.mapper.RefundMapper;
import com.example.orderservice.mapper.UserOrdersMapper;
import com.example.utils.IdempotentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class RefundMessageConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BalanceMapper balanceMapper;

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private IdempotentUtil idempotentUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = RabbitMQConstant.REFUND_QUEUE_NAME)
    @Transactional(rollbackFor = Exception.class)
    public void handleRefund(Refund refundMsg) {
        log.info("接收到退款消息：refundId={}", refundMsg.getRefundId());
        try {
            if (!idempotentUtil.firstAttempt("refund:" + refundMsg.getRefundId(), 86400)) {
                log.info("退款已处理，跳过：refundId={}", refundMsg.getRefundId());
                return;
            }
            // 1. 查询订单
            List<Orders> ordersList = jdbcTemplate.query(
                    "SELECT * FROM orders WHERE order_id = ?",
                    (rs, rowNum) -> {
                        Orders o = new Orders();
                        o.setOrderId(rs.getLong("order_id"));
                        o.setUserId(rs.getLong("user_id"));
                        o.setOrderStatus(rs.getInt("order_status"));
                        o.setPaymentMethod(rs.getInt("payment_method"));
                        o.setTotalAmount(rs.getBigDecimal("total_amount"));
                        return o;
                    }, refundMsg.getOrderId());
            if (ordersList.isEmpty() ||
                (ordersList.get(0).getOrderStatus() != OrderStatus.REFUNDING &&
                 ordersList.get(0).getOrderStatus() != OrderStatus.REFUNDED)) {
                log.error("订单状态异常：refundId={}", refundMsg.getRefundId());
                return;
            }
            Orders order = ordersList.get(0);
            // 2. 恢复库存
            List<OrderItems> items = ordersMapper.selectOrderItemsByOrderId(order.getOrderId());
            for (OrderItems item : items) {
                productMapper.restoreStock(item.getProductId(), item.getQuantity());
            }
            // 3. 余额支付则退还余额
            if (order.getPaymentMethod() == 2) {
                balanceMapper.increaseBalance(order.getUserId(), order.getTotalAmount());
            }
            // 4. 更新订单状态为已退款
            jdbcTemplate.update("UPDATE orders SET order_status = ?, update_time = NOW() WHERE order_id = ?",
                    OrderStatus.REFUNDED, order.getOrderId());
            // 5. 更新退款状态为已完成
            refundMapper.updateRefundStatus(refundMsg.getRefundId(), 3, LocalDateTime.now());
            log.info("退款处理完成：refundId={}, orderId={}", refundMsg.getRefundId(), order.getOrderId());
            stringRedisTemplate.delete("user:orderStats:" + order.getUserId());
            // 清除订单列表缓存
            java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + order.getUserId() + ":*");
            if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("处理退款消息失败，将重新入队：refundId={}", refundMsg.getRefundId(), e);
            throw e;  // 重新抛出异常，触发 MQ 重试机制，避免消息丢失
        }
    }
}
