package com.example.mailcoupon.consumer;

import com.example.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponCacheConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String AVAILABLE_KEY = "coupons:available";
    private static final String CONFIG_SPIN_COST_KEY = "lottery:config:spinCost";
    private static final String CONFIG_DAILY_LIMIT_KEY = "lottery:config:dailyLimit";
    private static final String PRIZES_CACHE_KEY = "lottery:prizes:active";

    @RabbitListener(queues = RabbitMQConstant.COUPON_CACHE_QUEUE_NAME)
    public void handleCacheRefresh(String message) {
        log.info("收到缓存刷新消息，清除相关缓存");
        stringRedisTemplate.delete(AVAILABLE_KEY);
        stringRedisTemplate.delete(CONFIG_SPIN_COST_KEY);
        stringRedisTemplate.delete(CONFIG_DAILY_LIMIT_KEY);
        stringRedisTemplate.delete(PRIZES_CACHE_KEY);
    }
}
