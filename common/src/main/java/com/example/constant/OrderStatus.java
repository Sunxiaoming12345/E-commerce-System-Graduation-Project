package com.example.constant;

public class OrderStatus {
    public static final Integer WAIT_PAYMENT = 0;
    public static final Integer PAID = 1;
    public static final Integer DELIVERED = 2;
    public static final Integer COMPLETED = 3;
    public static final Integer CANCELLED = 4;
    public static final Integer REFUNDING = 5;
    public static final Integer REFUNDED = 6;

    // 支付超时时间（秒）
    public static final Integer PAYMENT_TIMEOUT_SECONDS = 20;
}
