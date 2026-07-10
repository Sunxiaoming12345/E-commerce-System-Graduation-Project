package com.example.constant;

/**
 * 抽奖常量
 */
public class LotteryConstant {

    /** 奖品类型 — 优惠券 */
    public static final String PRIZE_COUPON = "COUPON";
    /** 奖品类型 — 余额 */
    public static final String PRIZE_BALANCE = "BALANCE";
    /** 奖品类型 — 谢谢参与 */
    public static final String PRIZE_THANKS = "THANKS";
    /** 奖品类型 — 实物 */
    public static final String PRIZE_PHYSICAL = "PHYSICAL";

    /** 发货状态 — 待发货 */
    public static final int FULFILLMENT_PENDING = 0;
    /** 发货状态 — 已发货 */
    public static final int FULFILLMENT_SHIPPED = 1;
    /** 发货状态 — 已收货 */
    public static final int FULFILLMENT_RECEIVED = 2;

    /** Redis — 每日抽奖分布式锁前缀 */
    public static final String REDIS_DAILY_LOCK_PREFIX = "lottery:daily:";
}
