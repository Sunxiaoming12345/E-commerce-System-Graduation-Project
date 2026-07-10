package com.example.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 缓存工具 — 库存独立 Redis key，直接 SET 更新，无并发问题
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_PREFIX = "product:stock:";
    private static final long STOCK_TTL = 1800;

    /**
     * 更新库存缓存（独立 key，直接 SET，与商品详情 Hash 解耦）
     */
    public void updateStockInCache(Long productId, int newStock) {
        try {
            stringRedisTemplate.opsForValue()
                    .set(STOCK_PREFIX + productId, String.valueOf(newStock),
                            STOCK_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("更新库存缓存失败 productId={}", productId);
        }
    }
}
