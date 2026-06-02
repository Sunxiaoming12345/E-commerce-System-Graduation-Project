package com.example.mailuser.config;

import com.example.constant.RabbitMQConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // ==================== 订单创建 ====================

    @Bean
    public DirectExchange orderCreateExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_CREATE_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(RabbitMQConstant.ORDER_CREATE_QUEUE_NAME)
                .deadLetterExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderCreateQueue())
                .to(orderCreateExchange())
                .with(RabbitMQConstant.ORDER_CREATE_ROUTING_KEY);
    }

    // ==================== 订单支付 ====================

    @Bean
    public DirectExchange orderPayExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_PAY_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue orderPayQueue() {
        return QueueBuilder.durable(RabbitMQConstant.ORDER_PAY_QUEUE_NAME)
                .deadLetterExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderPayBinding() {
        return BindingBuilder.bind(orderPayQueue())
                .to(orderPayExchange())
                .with(RabbitMQConstant.ORDER_PAY_ROUTING_KEY);
    }

    // ==================== 订单延迟（死信） ====================

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(RabbitMQConstant.DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY);
    }

    // ==================== 库存更新 ====================

    @Bean
    public DirectExchange stockExchange() {
        return new DirectExchange(RabbitMQConstant.STOCK_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.STOCK_QUEUE_NAME)
                .deadLetterExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding stockBinding() {
        return BindingBuilder.bind(stockQueue())
                .to(stockExchange())
                .with(RabbitMQConstant.STOCK_ROUTING_KEY);
    }

    // ==================== 退款 ====================

    @Bean
    public DirectExchange refundExchange() {
        return new DirectExchange(RabbitMQConstant.REFUND_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue refundQueue() {
        return QueueBuilder.durable(RabbitMQConstant.REFUND_QUEUE_NAME)
                .deadLetterExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding refundBinding() {
        return BindingBuilder.bind(refundQueue())
                .to(refundExchange())
                .with(RabbitMQConstant.REFUND_ROUTING_KEY);
    }
}
