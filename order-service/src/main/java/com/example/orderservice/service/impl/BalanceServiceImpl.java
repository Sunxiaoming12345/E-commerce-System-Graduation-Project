package com.example.orderservice.service.impl;
import com.example.exception.BusinessException;

import com.example.orderservice.entity.Balance;
import com.example.orderservice.mapper.BalanceMapper;
import com.example.orderservice.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 余额服务实现类
 *
 * @author sunxiaoming
 * @date 2026-02-27
 */
@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    @Autowired
    private BalanceMapper balanceMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取用户余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    @Override
    public Balance getBalance(Long userId) {
        Balance balance = balanceMapper.getBalanceByUserId(userId);
        if (balance == null) {
            // 如果余额记录不存在，初始化余额
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
        log.info("增加用户余额：userId={}, amount={}", userId, amount);
        stringRedisTemplate.delete("user:orderStats:" + userId);
        // 清除订单列表缓存
        java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + userId + ":*");
        if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        
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
        int affected = balanceMapper.decreaseBalance(userId, amount);
        if (affected == 0) {
            throw new BusinessException("余额不足");
        }
        log.info("减少用户余额：userId={}, amount={}", userId, amount);
        stringRedisTemplate.delete("user:orderStats:" + userId);
        // 清除订单列表缓存
        java.util.Set<String> keys = stringRedisTemplate.keys("user:orders:page:" + userId + ":*");
        if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        return getBalance(userId).getBalance();
    }

    @Override
    public boolean checkBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        return balance.getBalance().compareTo(amount) >= 0;
    }
}
