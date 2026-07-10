package com.example.orderservice.mapper;

import com.example.orderservice.entity.Balance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 余额Mapper
 *
 * @author sunxiaoming
 * @date 2026-02-27
 */
@Mapper
public interface BalanceMapper {

    /**
     * 根据用户ID获取余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    Balance getBalanceByUserId(@Param("userId") Long userId);

    /**
     * 创建余额记录
     *
     * @param balance 余额信息
     * @return 影响行数
     */
    int createBalance(Balance balance);

    /**
     * 更新余额
     *
     * @param userId 用户ID
     * @param balance 新余额
     * @return 影响行数
     */
    int updateBalance(@Param("userId") Long userId, @Param("balance") java.math.BigDecimal balance);

    /**
     * 增加余额
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 影响行数
     */
    int increaseBalance(@Param("userId") Long userId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 减少余额
     *
     * @param userId 用户ID
     * @param amount 减少金额
     * @return 影响行数
     */
    int decreaseBalance(@Param("userId") Long userId, @Param("amount") java.math.BigDecimal amount);
}
