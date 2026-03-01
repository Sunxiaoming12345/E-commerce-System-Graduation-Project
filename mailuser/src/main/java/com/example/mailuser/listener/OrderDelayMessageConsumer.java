package com.example.mailuser.listener;

import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.mapper.ProductMapper;
import com.example.mailuser.mapper.UserOrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderDelayMessageConsumer {

    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

        // 取消订单
        try {
            // 获取订单中的商品列表
            List<com.example.mailadmin.entity.OrderItems> orderItems = ordersMapper.selectOrderItemsByOrderId(orderId);

            // 恢复商品库存
            for (com.example.mailadmin.entity.OrderItems item : orderItems) {
                // 获取商品信息
                com.example.mailuser.vo.ProductVO productVO = productMapper.getProductById(item.getProductId());
                if (productVO != null) {
                    // 增加库存
                    int oldStock = productVO.getStock();
                    int newStock = oldStock + item.getQuantity();
                    productMapper.updateStock(item.getProductId(), newStock);
                    log.info("恢复商品库存：productId={}, 原库存={}, 增加数量={}, 新库存={}", item.getProductId(), oldStock, item.getQuantity(), newStock);
                    // 只有当库存从0到有时才发送消息
                    if (oldStock == 0 && newStock > 0) {
                        sendStockUpdateMessage(item.getProductId(), newStock);
                    }
                }
            }

            // 更新订单状态为已取消
            ordersMapper.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            log.info("订单已取消: {}", orderId);
        } catch (Exception e) {
            log.error("取消订单失败: {}", orderId, e);
        }
    }

    // 发送库存更新消息到RabbitMQ
    private void sendStockUpdateMessage(Long productId, Integer stock) {
        try {
            String message = "Product " + productId + " stock updated to " + stock;
            rabbitTemplate.convertAndSend(RabbitMQConstant.STOCK_EXCHANGE_NAME, RabbitMQConstant.STOCK_ROUTING_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}