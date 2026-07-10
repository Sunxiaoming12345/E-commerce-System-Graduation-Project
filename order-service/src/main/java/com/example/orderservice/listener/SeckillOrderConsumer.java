package com.example.orderservice.listener;

import com.example.constant.OrderStatus;
import com.example.constant.RabbitMQConstant;
import com.example.entity.SeckillMessageDTO;
import com.example.mailadmin.entity.Payments;
import com.example.orderservice.entity.Orders;
import com.example.orderservice.mapper.UserOrdersMapper;
import com.example.orderservice.mapper.UserPaymentMapper;
import com.example.orderservice.service.BalanceService;
import com.example.orderservice.utils.OrderNumberUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Component
public class SeckillOrderConsumer {

    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConstant.SECKILL_QUEUE_NAME)
    public void handleSeckillOrder(SeckillMessageDTO msg) {
        log.info("收到秒杀下单: userId={}, productId={}, seckillId={}, price={}",
                msg.getUserId(), msg.getProductId(), msg.getSeckillId(), msg.getPrice());

        try {
            // 1. 创建待支付订单
            Orders order = new Orders();
            order.setUserId(msg.getUserId().longValue());
            order.setOrderNumber(OrderNumberUtils.generate(msg.getUserId()));
            order.setOrderStatus(OrderStatus.WAIT_PAYMENT);
            order.setPaymentMethod(0);
            order.setTotalAmount(msg.getPrice());
            // 尝试使用用户历史收货信息
            Orders lastInfo = ordersMapper.selectLastReceiverInfo(msg.getUserId().longValue());
            order.setShippingAddress(lastInfo != null && lastInfo.getShippingAddress() != null
                    ? lastInfo.getShippingAddress() : "待填写");
            order.setReceiverName(lastInfo != null && lastInfo.getReceiverName() != null
                    ? lastInfo.getReceiverName() : "待填写");
            order.setReceiverPhone(lastInfo != null && lastInfo.getReceiverPhone() != null
                    ? lastInfo.getReceiverPhone() : "待填写");
            order.setCreateTime(LocalDateTime.now());
            ordersMapper.pay(order);

            // 2. 保存订单项
            String pname = msg.getProductName() != null && !msg.getProductName().isEmpty()
                    ? msg.getProductName() : "秒杀商品";
            ordersMapper.saveOrderItem(order.getOrderId(), msg.getProductId(),
                    pname, msg.getPrice(), 1, msg.getPrice());

            // 3. 创建待支付记录
            Payments payment = new Payments();
            payment.setOrderId(order.getOrderId());
            payment.setPaymentMethod(0);
            payment.setAmount(msg.getPrice());
            payment.setStatus(0);
            userPaymentMapper.insert(payment);

            // 4. 发送超时取消延迟消息
            long delayTime = OrderStatus.PAYMENT_TIMEOUT_SECONDS * 1000L;
            HashMap<String, Object> delayMsg = new HashMap<>();
            delayMsg.put("orderId", order.getOrderId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.DELAY_EXCHANGE_NAME,
                    RabbitMQConstant.DELAY_ROUTING_KEY,
                    delayMsg,
                    m -> {
                        m.getMessageProperties().setHeader("x-delay", delayTime);
                        return m;
                    }
            );

            // 5. 清除缓存
            clearOrderCache(msg.getUserId());

            log.info("秒杀订单创建成功: orderId={}, orderNo={}", order.getOrderId(), order.getOrderNumber());
        } catch (Exception e) {
            log.error("秒杀订单创建失败: userId={}, seckillId={}, 回滚库存", msg.getUserId(), msg.getSeckillId(), e);
            try {
                stringRedisTemplate.opsForValue().increment("seckill:stock:" + msg.getSeckillId());
                stringRedisTemplate.delete("seckill:bought:" + msg.getSeckillId() + ":" + msg.getUserId());
            } catch (Exception ignored) {}
        }
    }

    private void clearOrderCache(Integer userId) {
        try {
            stringRedisTemplate.delete("user:orderStats:" + userId);
            var keys = stringRedisTemplate.keys("user:orders:page:" + userId + ":*");
            if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        } catch (Exception ignored) {}
    }
}
