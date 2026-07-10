package com.example.orderservice.listener;
import com.example.exception.BusinessException;

import com.example.orderservice.dto.OrderCreateDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private com.example.utils.CacheUtil cacheUtil;

    @Autowired
    private IdempotentUtil idempotentUtil;

    @Autowired
    private CouponService couponService;

    @RabbitListener(queues = RabbitMQConstant.ORDER_CREATE_QUEUE_NAME)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderCreate(OrderCreateDTO orderCreateDTO) {
        log.info("接收到订单创建消息：orderCreateDTO={}", orderCreateDTO);

        // 幂等性校验：防止重复消费同一订单创建消息
        if (!idempotentUtil.firstAttempt("order:create:" + orderCreateDTO.getOrderNumber(), 300)) {
            return;
        }

        try {
            for (OrderCreateDTO.OrderItemDTO item : orderCreateDTO.getItems()) {
                ProductVO productVO = productMapper.getProductById(item.getProductId());
                if (productVO == null) {
                    log.error("商品不存在：productId={}", item.getProductId());
                    throw new BusinessException("商品不存在");
                }
                if (productVO.getStatus() == 0) {
                    log.error("商品已下架：productId={}", item.getProductId());
                    throw new BusinessException("商品已下架");
                }
                int affected = productMapper.updateStock(item.getProductId(), item.getQuantity());
                if (affected == 0) {
                    log.error("商品库存不足：productId={}, 需求={}", item.getProductId(), item.getQuantity());
                    throw new BusinessException("商品库存不足");
                }
                log.info("扣减商品库存：productId={}, 数量={}", item.getProductId(), item.getQuantity());
                // 查最新库存，同步到 Redis stock key
                ProductVO updated = productMapper.getProductById(item.getProductId());
                if (updated != null) {
                    cacheUtil.updateStockInCache(item.getProductId(), updated.getStock());
                    if (updated.getStock() == 0) {
                        sendStockUpdateMessage(item.getProductId(), 0, updated.getCategoryId());
                    }
                }
            }

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

            ordersMapper.pay(orders);

            for (OrderCreateDTO.OrderItemDTO item : orderCreateDTO.getItems()) {
                ProductVO productVO = productMapper.getProductById(item.getProductId());
                if (productVO != null) {
                    BigDecimal subtotal = productVO.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    ordersMapper.saveOrderItem(orders.getOrderId(), item.getProductId(), productVO.getName(), productVO.getPrice(), item.getQuantity(), subtotal);
                    log.info("保存订单项：orderId={}, productId={}, quantity={}", orders.getOrderId(), item.getProductId(), item.getQuantity());
                }
            }

            log.info("订单创建成功：orderId={}", orders.getOrderId());
            stringRedisTemplate.delete("user:orderStats:" + orders.getUserId());
            // 清除订单列表缓存
            java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + orders.getUserId() + ":*");
            if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);

            Payments payment = new Payments();
            payment.setOrderId(orders.getOrderId());
            payment.setPaymentMethod(orders.getPaymentMethod());
            payment.setAmount(orders.getTotalAmount());
            payment.setStatus(0);
            payment.setPayTime(null);
            userPaymentMapper.insert(payment);
            log.info("插入支付记录成功：paymentId={}, orderId={}", payment.getPaymentId(), orders.getOrderId());

            // 处理优惠券
            if (orderCreateDTO.getUserCouponId() != null) {
                try {
                    BigDecimal discount = couponService.useCoupon(
                            orderCreateDTO.getUserCouponId(),
                            orders.getOrderId(),
                            orderCreateDTO.getUserId().intValue(),
                            orders.getTotalAmount()
                    );
                    BigDecimal finalAmount = orders.getTotalAmount().subtract(discount);
                    ordersMapper.updateOrderTotalAmount(orders.getOrderId(), finalAmount);
                    userPaymentMapper.updateAmount(payment.getPaymentId(), finalAmount);
                    log.info("优惠券抵扣成功：userCouponId={}, discount={}, finalAmount={}",
                            orderCreateDTO.getUserCouponId(), discount, finalAmount);
                } catch (Exception e) {
                    log.error("优惠券使用失败：userCouponId={}, error={}",
                            orderCreateDTO.getUserCouponId(), e.getMessage());
                    throw new BusinessException("优惠券使用失败：" + e.getMessage(), e);
                }
            }

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
            throw new BusinessException("订单创建失败：" + e.getMessage(), e);
        }
    }

    private void sendStockUpdateMessage(Long productId, Integer stock, Long categoryId) {
        try {
            String message = productId + "," + stock + "," + categoryId;
            rabbitTemplate.convertAndSend(RabbitMQConstant.STOCK_EXCHANGE_NAME, RabbitMQConstant.STOCK_ROUTING_KEY, message);
            log.info("发送库存更新消息：productId={}, stock={}, categoryId={}", productId, stock, categoryId);
        } catch (Exception e) {
            log.error("发送库存更新消息失败：", e);
        }
    }
}
