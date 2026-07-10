package com.example.config;

import com.example.constant.RabbitMQConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 基础设施配置（共享模块）
 * 所有引入 spring-boot-starter-amqp 的服务启动时自动声明交换机/队列/绑定。
 * @ConditionalOnClass 确保未引入 AMQP 的服务（如 gateway）不会加载此配置。
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
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

    // ==================== 抽奖缓存更新 ====================

    @Bean
    public DirectExchange lotteryExchange() {
        return new DirectExchange(RabbitMQConstant.LOTTERY_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue lotteryQueue() {
        return new Queue(RabbitMQConstant.LOTTERY_QUEUE_NAME, true);
    }

    @Bean
    public Binding lotteryBinding() {
        return BindingBuilder.bind(lotteryQueue())
                .to(lotteryExchange())
                .with(RabbitMQConstant.LOTTERY_ROUTING_KEY);
    }

    // ==================== 优惠券缓存更新 ====================

    @Bean
    public DirectExchange couponCacheExchange() {
        return new DirectExchange(RabbitMQConstant.COUPON_CACHE_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue couponCacheQueue() {
        return new Queue(RabbitMQConstant.COUPON_CACHE_QUEUE_NAME, true);
    }

    @Bean
    public Binding couponCacheBinding() {
        return BindingBuilder.bind(couponCacheQueue())
                .to(couponCacheExchange())
                .with(RabbitMQConstant.COUPON_CACHE_ROUTING_KEY);
    }

    // ==================== 秒杀削峰 ====================

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(RabbitMQConstant.SECKILL_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SECKILL_QUEUE_NAME)
                .deadLetterExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with(RabbitMQConstant.SECKILL_ROUTING_KEY);
    }

    // ==================== 订单延迟（TTL + DLX） ====================

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DELAY_QUEUE_NAME)
                .deadLetterExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }
}
