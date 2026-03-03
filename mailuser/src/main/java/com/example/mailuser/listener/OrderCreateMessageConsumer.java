package com.example.mailuser.listener;

import com.example.mailuser.dto.OrderCreateDTO;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.mapper.ProductMapper;
import com.example.mailuser.mapper.UserOrdersMapper;
import com.example.mailuser.vo.ProductVO;
import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.mailuser.mapper.UserPaymentMapper;
import com.example.mailadmin.entity.Payments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

import static com.example.constant.RabbitMQConstant.ORDER_CREATE_QUEUE_NAME;

@Slf4j
@Component
public class OrderCreateMessageConsumer {

    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConstant.ORDER_CREATE_QUEUE_NAME)
    public void handleOrderCreate(OrderCreateDTO orderCreateDTO) {
        log.info("接收到订单创建消息：orderCreateDTO={}", orderCreateDTO);

        try {
            // 检查库存并减少库存
            for (OrderCreateDTO.OrderItemDTO item : orderCreateDTO.getItems()) {
                // 获取商品信息
                ProductVO productVO = productMapper.getProductById(item.getProductId());
                if (productVO == null) {
                    log.error("商品不存在：productId={}", item.getProductId());
                    throw new RuntimeException("商品不存在");
                }
                // 检查商品是否已下架
                if (productVO.getStatus() == 0) {
                    log.error("商品已下架：productId={}", item.getProductId());
                    throw new RuntimeException("商品已下架");
                }
                // 检查库存是否足够
                if (productVO.getStock() < item.getQuantity()) {
                    log.error("商品库存不足：productId={}, 库存={}, 需求={}", item.getProductId(), productVO.getStock(), item.getQuantity());
                    throw new RuntimeException("商品库存不足");
                }
                // 减少库存
                int oldStock = productVO.getStock();
                int newStock = oldStock - item.getQuantity();
                productMapper.updateStock(item.getProductId(), newStock);
                log.info("减少商品库存：productId={}, 原库存={}, 减少数量={}, 新库存={}", item.getProductId(), oldStock, item.getQuantity(), newStock);
                // 只有当库存从有到0时才发送消息
                if (oldStock > 0 && newStock == 0) {
                    sendStockUpdateMessage(item.getProductId(), newStock, productVO.getCategoryId());
                }
            }

            // 创建订单记录
            Orders orders = new Orders();
            orders.setUserId(orderCreateDTO.getUserId());
            orders.setOrderNumber(orderCreateDTO.getOrderNumber());
            orders.setOrderStatus(OrderStatus.WAIT_PAYMENT);
            orders.setPaymentMethod(orderCreateDTO.getPaymentMethod());
            orders.setTotalAmount(orderCreateDTO.getTotalAmount());
            orders.setShippingAddress(orderCreateDTO.getShippingAddress());
            orders.setReceiverName(orderCreateDTO.getReceiverName());
            orders.setReceiverPhone(orderCreateDTO.getReceiverPhone());
            orders.setCreateTime(LocalDateTime.now());

            // 保存订单
            ordersMapper.pay(orders);

            // 保存订单项
            for (OrderCreateDTO.OrderItemDTO item : orderCreateDTO.getItems()) {
                // 获取商品信息
                ProductVO productVO = productMapper.getProductById(item.getProductId());
                if (productVO != null) {
                    // 计算小计金额
                    BigDecimal subtotal = productVO.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    // 保存订单项
                    ordersMapper.saveOrderItem(orders.getOrderId(), item.getProductId(), productVO.getName(), productVO.getPrice(), item.getQuantity(), subtotal);
                    log.info("保存订单项：orderId={}, productId={}, quantity={}", orders.getOrderId(), item.getProductId(), item.getQuantity());
                }
            }

            log.info("订单创建成功：orderId={}", orders.getOrderId());
            
            // 插入支付记录，状态为待付款
            Payments payment = new Payments();
            payment.setOrderId(orders.getOrderId());
            payment.setPaymentMethod(orders.getPaymentMethod());
            payment.setAmount(orders.getTotalAmount());
            payment.setStatus(0); // 0-待付款
            payment.setPayTime(null);
            userPaymentMapper.insert(payment);
            log.info("插入支付记录成功：paymentId={}, orderId={}", payment.getPaymentId(), orders.getOrderId());
            
            // 发送延迟消息，一定时间后检查订单是否支付
            long delayTime = OrderStatus.PAYMENT_TIMEOUT_SECONDS * 1000L;
            HashMap<String, Object> message = new HashMap<>();
            message.put("orderId", orders.getOrderId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.DELAY_EXCHANGE_NAME,
                    RabbitMQConstant.DELAY_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setHeader("x-delay", delayTime);
                        return msg;
                    }
            );
            log.info("发送订单延迟消息：orderId={}, delayTime={}ms", orders.getOrderId(), delayTime);
        } catch (Exception e) {
            log.error("处理订单创建消息失败：", e);
            // 这里可以添加重试机制或错误处理逻辑
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
