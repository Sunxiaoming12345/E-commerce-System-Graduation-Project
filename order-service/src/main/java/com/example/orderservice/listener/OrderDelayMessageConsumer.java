package com.example.orderservice.listener;

import com.example.orderservice.entity.Orders;
import com.example.orderservice.mapper.ProductMapper;
import com.example.orderservice.mapper.UserOrdersMapper;
import com.example.orderservice.vo.ProductVO;
import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.orderservice.mapper.UserPaymentMapper;
import com.example.mailadmin.entity.Payments;
import com.example.orderservice.service.CouponService;
import com.example.utils.IdempotentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderDelayMessageConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    @Autowired
    private IdempotentUtil idempotentUtil;

    @Autowired
    private com.example.utils.CacheUtil cacheUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CouponService couponService;

    /**
     * 处理延迟消息，检查并取消超时订单
     * @param message 消息内容
     */
    @RabbitListener(queues = RabbitMQConstant.DELAY_QUEUE_NAME)
    public void handleOrderDelayMessage(java.util.Map<String, Object> message) {
        Object orderIdObj = message.get("orderId");
        Long orderId = null;
        if (orderIdObj instanceof Integer) {
            orderId = ((Integer) orderIdObj).longValue();
        } else if (orderIdObj instanceof Long) {
            orderId = (Long) orderIdObj;
        }
        log.info("收到延迟消息，订单ID: {}", orderId);

        // 检查orderId是否为null
        if (orderId == null) {
            log.error("订单ID为null，无法处理延迟消息");
            return;
        }

        // 查询订单信息
        Orders order = ordersMapper.getOrderById(orderId);
        if (order == null) {
            log.info("订单不存在: {}", orderId);
            return;
        }

        // 只有待支付的订单需要处理
        if (order.getOrderStatus() != OrderStatus.WAIT_PAYMENT) {
            log.info("订单状态不是待支付，无需处理: {}", orderId);
            return;
        }

        // 幂等性校验：防止 MQ 重试导致重复恢复库存
        if (!idempotentUtil.firstAttempt("order:cancel:" + orderId, 300)) {
            return;
        }

        // 取消订单
        try {
            // 获取订单中的商品列表
            List<com.example.mailadmin.entity.OrderItems> orderItems = ordersMapper.selectOrderItemsByOrderId(orderId);

            // 恢复商品库存（原子操作）
            for (com.example.mailadmin.entity.OrderItems item : orderItems) {
                productMapper.restoreStock(item.getProductId(), item.getQuantity());
                log.info("恢复商品库存：productId={}, 数量={}", item.getProductId(), item.getQuantity());
                com.example.orderservice.vo.ProductVO updated = productMapper.getProductById(item.getProductId());
                if (updated != null) {
                    cacheUtil.updateStockInCache(item.getProductId(), updated.getStock());
                    if (updated.getStock() > 0) {
                        sendStockUpdateMessage(item.getProductId(), updated.getStock(), updated.getCategoryId());
                    }
                }
            }

            // 更新订单状态为已取消
            ordersMapper.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            
            // 更新支付记录状态为已取消
            Payments payment = userPaymentMapper.getByOrderId(orderId);
            if (payment != null) {
                userPaymentMapper.updateStatus(payment.getPaymentId(), 2); // 2-已取消
                log.info("支付记录已取消：paymentId={}, orderId={}", payment.getPaymentId(), orderId);
            }

            // 释放优惠券（如果使用了优惠券）
            try {
                couponService.releaseCoupon(orderId);
            } catch (Exception e) {
                log.error("释放优惠券失败：orderId={}", orderId, e);
            }

            log.info("订单已取消: {}", orderId);
            stringRedisTemplate.delete("user:orderStats:" + order.getUserId());
            // 清除订单列表缓存
            java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + order.getUserId() + ":*");
            if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("取消订单失败: {}", orderId, e);
        }
    }

    // 发送库存更新消息到RabbitMQ
    private void sendStockUpdateMessage(Long productId, Integer stock, Long categoryId) {
        try {
            // 构建包含商品ID、库存和分类ID的消息
            String message = productId + "," + stock + "," + categoryId;
            rabbitTemplate.convertAndSend(RabbitMQConstant.STOCK_EXCHANGE_NAME, RabbitMQConstant.STOCK_ROUTING_KEY, message);
            log.info("发送库存更新消息：productId={}, stock={}, categoryId={}", productId, stock, categoryId);
        } catch (Exception e) {
            log.error("发送库存更新消息失败：", e);
        }
    }
}