package com.example.constant;

public class SeckillConstant {
    /** Redis key: 秒杀库存 seckill:stock:{seckillId} */
    public static final String STOCK_KEY_PREFIX = "seckill:stock:";

    /** Redis key: 秒杀商品信息缓存 seckill:product:{seckillId} */
    public static final String PRODUCT_KEY_PREFIX = "seckill:product:";

    /** Redis key: 用户购买标记（防重复购买） seckill:bought:{seckillId}:{userId} */
    public static final String BOUGHT_KEY_PREFIX = "seckill:bought:";

    /** 秒杀状态 */
    public static final int STATUS_UPCOMING = 1;   // 未开始
    public static final int STATUS_ACTIVE = 2;     // 进行中
    public static final int STATUS_ENDED = 3;      // 已结束

    /** 秒杀下单消息 */
    public static final String SECKILL_ORDER_TOPIC = "seckill.order.topic";
}
