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

    // 订单创建交换机
    public static final String ORDER_CREATE_EXCHANGE_NAME = "order.create.exchange";
    // 订单创建队列
    public static final String ORDER_CREATE_QUEUE_NAME = "order.create.queue";
    // 订单创建路由键
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create.routing.key";

    // 订单支付交换机
    public static final String ORDER_PAY_EXCHANGE_NAME = "order.pay.exchange";
    // 订单支付队列
    public static final String ORDER_PAY_QUEUE_NAME = "order.pay.queue";
    // 订单支付路由键
    public static final String ORDER_PAY_ROUTING_KEY = "order.pay.routing.key";

    // 死信交换机
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";
    // 死信队列
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    // 死信路由键
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter.routing.key";

    // 退款交换机
    public static final String REFUND_EXCHANGE_NAME = "refund.exchange";
    // 退款队列
    public static final String REFUND_QUEUE_NAME = "refund.queue";
    // 退款路由键
    public static final String REFUND_ROUTING_KEY = "refund.routing.key";

}
