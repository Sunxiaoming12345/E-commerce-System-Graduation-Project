package com.example.userservice.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.exception.BusinessException;

import com.example.userservice.entity.Balance;
import com.example.userservice.mapper.BalanceMapper;
import com.example.userservice.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 余额服务实现类
 *
 * @author sunxiaoming
 * @date 2026-02-27
 */
@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private static final String ORDER_STATS_PREFIX = "user:orderStats:";
    private static final long ORDER_STATS_TTL = 10;
    private static final TimeUnit ORDER_STATS_TTL_UNIT = TimeUnit.MINUTES;

    @Autowired
    private BalanceMapper balanceMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取用户余额 — 优先从 Redis (user:orderStats:{userId}) 读取
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    @Override
    public Balance getBalance(Long userId) {
        // 1. 尝试从 Redis 缓存读取（与 order-service 共用同一份数据）
        String key = ORDER_STATS_PREFIX + userId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            JSONObject stats = JSON.parseObject(cached);
            BigDecimal balance = stats.getBigDecimal("balance");
            if (balance != null) {
                Balance b = new Balance();
                b.setUserId(userId);
                b.setBalance(balance);
                return b;
            }
        }

        // 2. 缓存未命中，查 DB
        Balance balance = balanceMapper.getBalanceByUserId(userId);
        if (balance == null) {
            balance = initBalance(userId);
        }
        return balance;
    }

    /**
     * 初始化用户余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    @Override
    public Balance initBalance(Long userId) {
        Balance balance = new Balance();
        balance.setUserId(userId);
        balance.setBalance(BigDecimal.ZERO);
        balanceMapper.createBalance(balance);
        log.info("初始化用户余额：userId={}", userId);
        return balance;
    }

    /**
     * 增加余额
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 新余额
     */
    @Override
    public BigDecimal increaseBalance(Long userId, BigDecimal amount) {
        // 确保余额记录存在
        getBalance(userId);

        // 增加余额
        balanceMapper.increaseBalance(userId, amount);
        // 删除 Redis 缓存，让下次查询走 DB 拿最新值
        stringRedisTemplate.delete(ORDER_STATS_PREFIX + userId);
        log.info("增加用户余额：userId={}, amount={}", userId, amount);

        // 返回最新余额
        return getBalance(userId).getBalance();
    }

    /**
     * 减少余额
     *
     * @param userId 用户ID
     * @param amount 减少金额
     * @return 新余额
     */
    @Override
    public BigDecimal decreaseBalance(Long userId, BigDecimal amount) {
        // 检查余额是否足够
        if (!checkBalance(userId, amount)) {
            throw new BusinessException("余额不足");
        }

        // 减少余额
        balanceMapper.decreaseBalance(userId, amount);
        // 删除 Redis 缓存，让下次查询走 DB 拿最新值
        stringRedisTemplate.delete(ORDER_STATS_PREFIX + userId);
        log.info("减少用户余额：userId={}, amount={}", userId, amount);

        // 返回最新余额
        return getBalance(userId).getBalance();
    }

    /**
     * 检查余额是否足够
     *
     * @param userId 用户ID
     * @param amount 需要的金额
     * @return 是否足够
     */
    @Override
    public boolean checkBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        return balance.getBalance().compareTo(amount) >= 0;
    }
}
