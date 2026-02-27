package com.example.mailuser.service.impl;

import com.example.mailuser.entity.Balance;
import com.example.mailuser.mapper.BalanceMapper;
import com.example.mailuser.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
            throw new RuntimeException("余额不足");
        }
        
        // 减少余额
        balanceMapper.decreaseBalance(userId, amount);
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
