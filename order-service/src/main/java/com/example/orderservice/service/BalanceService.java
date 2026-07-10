package com.example.orderservice.service;

import com.example.orderservice.entity.Balance;

import java.math.BigDecimal;

/**
 * 余额服务接口
 *
 * @author sunxiaoming
 * @date 2026-02-27
 */
public interface BalanceService {

    /**
     * 获取用户余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    Balance getBalance(Long userId);

    /**
     * 初始化用户余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    Balance initBalance(Long userId);

    /**
     * 增加余额
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 新余额
     */
    BigDecimal increaseBalance(Long userId, BigDecimal amount);

    /**
     * 减少余额
     *
     * @param userId 用户ID
     * @param amount 减少金额
     * @return 新余额
     */
    BigDecimal decreaseBalance(Long userId, BigDecimal amount);

    /**
     * 检查余额是否足够
     *
     * @param userId 用户ID
     * @param amount 需要的金额
     * @return 是否足够
     */
    boolean checkBalance(Long userId, BigDecimal amount);
}
