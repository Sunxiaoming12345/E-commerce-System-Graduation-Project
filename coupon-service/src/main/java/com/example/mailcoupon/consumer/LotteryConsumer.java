package com.example.mailcoupon.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LotteryConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String RECORDS_CACHE_KEY_PREFIX = "lottery:records:";
    private static final String RECORDS_COUNT_KEY_PREFIX = "lottery:records:count:";

    @RabbitListener(queues = RabbitMQConstant.LOTTERY_QUEUE_NAME)
    public void handleCacheRefresh(String message) {
        JSONObject msg = JSON.parseObject(message);
        Integer userId = msg.getInteger("userId");
        log.info("收到抽奖缓存刷新消息: userId={}", userId);

        // 清除该用户所有抽奖记录分页缓存
        for (int page = 1; page <= 20; page++) {
            for (int size : new int[]{5, 10, 20, 50}) {
                stringRedisTemplate.delete(RECORDS_CACHE_KEY_PREFIX + userId + ":" + page + ":" + size);
            }
        }
        stringRedisTemplate.delete(RECORDS_COUNT_KEY_PREFIX + userId);
        log.info("抽奖记录缓存已清除: userId={}", userId);
    }
}
