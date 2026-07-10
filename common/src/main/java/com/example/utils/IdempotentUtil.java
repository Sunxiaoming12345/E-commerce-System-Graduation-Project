package com.example.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 幂等性校验
 * <p>
 * 基于 SETNX 原子操作，确保同一业务键仅被处理一次。
 * 用于 MQ 消费端的重复消息防护——RabbitMQ 投递语义为 at-least-once，
 * 消费者崩溃后重试会导致同一消息被重复消费。
 * <p>
 * 用法示例：
 * <pre>
 *   if (!idempotentUtil.firstAttempt("order:create:" + orderNumber, 86400)) {
 *       log.warn("重复消息，跳过");
 *       return;
 *   }
 *   // 执行业务逻辑
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String PREFIX = "idempotent:";

    /**
     * 首次尝试标记，原子操作
     *
     * @param bizKey    业务唯一键（如 order:create:NO20240101001）
     * @param ttlSeconds 标记存活时间（秒），应覆盖消息重试窗口，建议 86400（24小时）
     * @return true = 首次处理，可继续执行；false = 重复消息，应跳过
     */
    public boolean firstAttempt(String bizKey, long ttlSeconds) {
        boolean success = Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue()
                        .setIfAbsent(PREFIX + bizKey, "1", ttlSeconds, TimeUnit.SECONDS)
        );
        if (!success) {
            log.warn("检测到重复消息，跳过处理：bizKey={}", bizKey);
        }
        return success;
    }

    /**
     * 签发一次性幂等令牌
     * <p>
     * 用于防止表单重复提交。页面加载时调用，返回 token，
     * 提交业务时携带该 token，由 {@link #consumeToken} 消费。
     *
     * @param bizKey     业务场景（如 submit-order）
     * @param ttlSeconds 令牌有效期（秒），建议 300（5分钟）
     * @return 令牌字符串
     */
    public String reserveToken(String bizKey, long ttlSeconds) {
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue()
                .set(PREFIX + bizKey + ":" + token, "1", ttlSeconds, TimeUnit.SECONDS);
        return token;
    }

    /**
     * 消费一次性令牌，使用 DEL 原子操作
     * <p>
     * DEL 返回删除的 key 数量：1 = 首次使用且删除成功，0 = key 不存在（已用过或已过期）。
     * 一次网络往返完成"检验 + 销毁"，无需 Lua 脚本。
     *
     * @param bizKey 业务场景（需与签发时一致）
     * @param token  令牌
     * @return true = 令牌有效已消费，false = 无效或已使用
     */
    public boolean consumeToken(String bizKey, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Boolean deleted = stringRedisTemplate.delete(PREFIX + bizKey + ":" + token);
        boolean valid = Boolean.TRUE.equals(deleted);
        if (!valid) {
            log.warn("令牌无效或已被使用：bizKey={}, token={}", bizKey, token);
        }
        return valid;
    }
}
