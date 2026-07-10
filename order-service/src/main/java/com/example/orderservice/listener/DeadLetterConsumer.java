package com.example.orderservice.listener;

import com.example.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 死信队列消费者，仅记录日志便于排查问题
 */
@Slf4j
@Component
public class DeadLetterConsumer {

    @RabbitListener(queues = RabbitMQConstant.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(Message message) {
        log.error("=== 死信消息 ===");
        log.error("RoutingKey: {}", message.getMessageProperties().getReceivedRoutingKey());
        log.error("Body: {}", new String(message.getBody()));
        log.error("Headers: {}", message.getMessageProperties().getHeaders());
    }
}
