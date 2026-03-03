package com.example.mailuser.listener;

import com.example.mailadmin.entity.Payments;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.mapper.UserOrdersMapper;
import com.example.mailuser.service.BalanceService;
import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.mailuser.mapper.UserPaymentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderPayMessageConsumer {

    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    @RabbitListener(queues = RabbitMQConstant.ORDER_PAY_QUEUE_NAME)
    public void handleOrderPay(PayDTO payDTO) {
        log.info("接收到订单支付消息：payDTO={}", payDTO);

        try {
            Long userId = payDTO.getUserId();
            
            // 验证订单是否存在且属于当前用户
            Orders order = ordersMapper.getOrderByIdAndUserId(payDTO.getOrderId(), userId);
            
            // 如果通过ID查询不到，尝试通过订单号查询
            if (order == null) {
                // 将orderId转换为字符串作为订单号查询
                String orderNumber = payDTO.getOrderId().toString();
                order = ordersMapper.selectByOrderNumber(orderNumber);
                
                // 再次检查订单是否存在且属于当前用户
                if (order == null || !order.getUserId().equals(userId)) {
                    log.error("订单不存在：orderId={}, userId={}", payDTO.getOrderId(), userId);
                    return; // 直接返回，不抛出异常
                }
            }
            
            // 只有待支付的订单可以支付
            if (order.getOrderStatus() != OrderStatus.WAIT_PAYMENT) {
                log.error("只能支付待支付的订单：orderId={}, orderStatus={}", order.getOrderId(), order.getOrderStatus());
                return; // 直接返回，不抛出异常
            }
            
            // 如果是余额支付，检查余额是否足够
            if (payDTO.getPaymentMethod() == 2) {
                boolean isEnough = balanceService.checkBalance(userId, order.getTotalAmount());
                if (!isEnough) {
                    log.error("余额不足：userId={}, amount={}", userId, order.getTotalAmount());
                    return; // 直接返回，不抛出异常
                }
                // 扣除余额
                try {
                    balanceService.decreaseBalance(userId, order.getTotalAmount());
                    log.info("余额支付成功：userId={}, amount={}", userId, order.getTotalAmount());
                } catch (Exception e) {
                    log.error("扣除余额失败：userId={}, amount={}", userId, order.getTotalAmount(), e);
                    return; // 直接返回，不抛出异常
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
        } catch (Exception e) {
            log.error("处理订单支付消息失败：", e);
            // 这里可以添加重试机制或错误处理逻辑
        }
    }
}
