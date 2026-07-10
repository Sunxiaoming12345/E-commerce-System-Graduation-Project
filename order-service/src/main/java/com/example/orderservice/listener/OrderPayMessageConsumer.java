package com.example.orderservice.listener;

import com.example.mailadmin.entity.Payments;
import com.example.orderservice.dto.PayDTO;
import com.example.orderservice.entity.Orders;
import com.example.orderservice.mapper.UserOrdersMapper;
import com.example.orderservice.service.BalanceService;
import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.orderservice.mapper.UserPaymentMapper;
import com.example.utils.IdempotentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderPayMessageConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    @Autowired
    private IdempotentUtil idempotentUtil;

    @RabbitListener(queues = RabbitMQConstant.ORDER_PAY_QUEUE_NAME)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderPay(PayDTO payDTO) {
        log.info("接收到订单支付消息：payDTO={}", payDTO);

        try {
            Long userId = payDTO.getUserId();

            Orders order = null;
            String orderNumber = payDTO.getOrderNumber();
            if (orderNumber != null && !orderNumber.isBlank()) {
                order = ordersMapper.selectByOrderNumber(orderNumber.trim());
            } else if (payDTO.getOrderId() != null) {
                order = ordersMapper.getOrderByIdAndUserId(payDTO.getOrderId(), userId);
                if (order == null) {
                    order = ordersMapper.selectByOrderNumber(payDTO.getOrderId().toString());
                }
            }
            if (order == null || !order.getUserId().equals(userId)) {
                log.error("订单不存在：orderId={}, orderNumber={}, userId={}",
                        payDTO.getOrderId(), payDTO.getOrderNumber(), userId);
                return;
            }
            
            // 只有待支付的订单可以支付
            if (order.getOrderStatus() != OrderStatus.WAIT_PAYMENT) {
                log.error("只能支付待支付的订单：orderId={}, orderStatus={}", order.getOrderId(), order.getOrderStatus());
                return;
            }

            // 幂等性校验：防止余额扣减和状态更新之间崩溃导致重复扣款
            if (!idempotentUtil.firstAttempt("order:pay:" + order.getOrderId(), 300)) {
                return;
            }

            // 余额支付（decreaseBalance 内 SQL 原子校验，防止超扣）
            if (payDTO.getPaymentMethod() == 2) {
                try {
                    balanceService.decreaseBalance(userId, order.getTotalAmount());
                    log.info("余额支付成功：userId={}, amount={}", userId, order.getTotalAmount());
                } catch (Exception e) {
                    log.error("扣除余额失败：userId={}, amount={}", userId, order.getTotalAmount(), e);
                    return;
                }
            }
            
            // 更新订单状态为已支付，并更新支付方式
            order.setOrderStatus(OrderStatus.PAID);
            order.setPaymentMethod(payDTO.getPaymentMethod());
            order.setPayTime(LocalDateTime.now());
            
            // 这里可以添加其他支付方式的逻辑，比如调用第三方支付接口等
            
            // 更新订单支付信息
            ordersMapper.updateOrderForPayment(order.getOrderId(), OrderStatus.PAID, payDTO.getPaymentMethod());
            
            // 更新支付记录状态为已付款
            Payments payment = userPaymentMapper.getByOrderId(order.getOrderId());
            if (payment != null) {
                userPaymentMapper.updateStatus(payment.getPaymentId(), 1); // 1-已付款
                log.info("支付记录更新成功：paymentId={}, orderId={}, status=已付款", payment.getPaymentId(), order.getOrderId());
            }
            log.info("订单支付成功：orderId={}, paymentMethod={}", order.getOrderId(), payDTO.getPaymentMethod());
            stringRedisTemplate.delete("user:orderStats:" + order.getUserId());
            // 清除订单列表缓存
            java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + order.getUserId() + ":*");
            if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("处理订单支付消息失败：", e);
            // 这里可以添加重试机制或错误处理逻辑
        }
    }
}
