package com.example.constant;

public class RabbitMQConstant {
    public static final String DELAY_EXCHANGE_NAME = "order.delay.exchange";
    // 延迟队列名称
    public static final String DELAY_QUEUE_NAME = "order.delay.queue";
    // 路由键
    public static final String DELAY_ROUTING_KEY = "order.delay.routing.key";

    // 库存更新交换机
    public static final String STOCK_EXCHANGE_NAME = "stock.exchange";
    // 库存更新队列
    public static final String STOCK_QUEUE_NAME = "stock.queue";
    // 库存更新路由键
    public static final String STOCK_ROUTING_KEY = "stock.update.routing.key";

}
