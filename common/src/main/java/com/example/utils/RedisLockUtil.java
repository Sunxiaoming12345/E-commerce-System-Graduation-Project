package com.example.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁
 * <p>
 * 基于 SET NX PX + Lua 脚本安全释放，适用于多实例部署场景下的并发控制。
 * 锁的粒度：建议按资源 ID 加锁，如 lock:product:stock:1001。
 * <p>
 * 注意：单 MQ 消费者（concurrency=1）场景下不需要此锁；当开启并发消费或多实例时生效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "lock:";

    /**
     * Lua 脚本：原子校验锁的值并删除，防止误删他人的锁
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "return 0 " +
                        "end"
        );
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    /**
     * 尝试获取锁
     *
     * @param resourceId 资源标识（如 productId）
     * @param ttlSeconds 锁超时时间（秒），建议不超过 30 秒
     * @return 锁令牌，获取失败返回 null
     */
    public String acquire(String resourceId, long ttlSeconds) {
        String key = LOCK_PREFIX + resourceId;
        String token = UUID.randomUUID().toString().replace("-", "");
        boolean success = Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue()
                        .setIfAbsent(key, token, ttlSeconds, TimeUnit.SECONDS)
        );
        if (success) {
            log.debug("获取分布式锁成功：key={}", key);
            return token;
        }
        log.warn("获取分布式锁失败：key={}", key);
        return null;
    }

    /**
     * 释放锁
     *
     * @param resourceId 资源标识
     * @param token      获取锁时返回的令牌
     */
    public void release(String resourceId, String token) {
        if (token == null) {
            return;
        }
        String key = LOCK_PREFIX + resourceId;
        Long result = stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(key),
                token
        );
        if (result != null && result == 1) {
            log.debug("释放分布式锁成功：key={}", key);
        } else {
            log.warn("释放分布式锁失败（可能已过期或被他人持有）：key={}", key);
        }
    }

    /**
     * 快捷方法：尝试获取锁，最多等待 waitSeconds 秒
     *
     * @return 锁令牌，超时返回 null
     */
    public String acquireWithRetry(String resourceId, long ttlSeconds, long waitSeconds) {
        long deadline = System.currentTimeMillis() + waitSeconds * 1000;
        String token;
        do {
            token = acquire(resourceId, ttlSeconds);
            if (token != null) {
                return token;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        } while (System.currentTimeMillis() < deadline);
        return null;
    }
}
