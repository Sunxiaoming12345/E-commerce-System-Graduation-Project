package com.example.userservice.controller;

import com.example.context.BaseContext;
import com.example.userservice.service.BalanceService;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 余额控制器
 *
 * @author sunxiaoming
 * @date 2026-02-27
 */
@RestController
@RequestMapping("/user/balance")
@Api(tags = "余额管理")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    /**
     * 获取用户余额
     *
     * @return 余额信息
     */
    @GetMapping("/info")
    @ApiOperation(value = "获取用户余额", notes = "获取当前用户的余额信息")
    public Result<BigDecimal> getBalance() {
        Long userId = BaseContext.getCurrentId();
        BigDecimal balance = balanceService.getBalance(userId).getBalance();
        return Result.success(balance);
    }

    /**
     * 充值余额
     *
     * @param amount 充值金额
     * @return 新余额
     */
    @PostMapping("/recharge")
    @ApiOperation(value = "充值余额", notes = "为当前用户充值余额")
    public Result<BigDecimal> recharge(@ApiParam(name = "amount", value = "充值金额", required = true) @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        Long userId = BaseContext.getCurrentId();
        BigDecimal newBalance = balanceService.increaseBalance(userId, amount);
        return Result.success(newBalance);
    }

    /**
     * 检查余额是否足够
     *
     * @param amount 需要的金额
     * @return 是否足够
     */
    @GetMapping("/check")
    @ApiOperation(value = "检查余额是否足够", notes = "检查当前用户余额是否足够支付指定金额")
    public Result<Boolean> checkBalance(@ApiParam(name = "amount", value = "需要的金额", required = true) @RequestParam BigDecimal amount) {
        Long userId = BaseContext.getCurrentId();
        boolean isEnough = balanceService.checkBalance(userId, amount);
        return Result.success(isEnough);
    }

    /**
     * 初始化用户余额
     * 用于用户注册时调用
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/init")
    @ApiOperation(value = "初始化用户余额", notes = "为指定用户初始化余额")
    public Result<Void> initBalance(@ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId) {
        balanceService.initBalance(userId);
        return Result.success();
    }

    // ==================== 内部接口（服务间调用，不走网关） ====================

    /**
     * 内部接口 — 扣减余额（由 coupon-service 抽奖调用）
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     * @return 新余额
     */
    @PostMapping("/internal/decrease")
    @ApiOperation(value = "内部扣减余额", notes = "由其他微服务调用，扣除指定用户余额")
    public Result<BigDecimal> internalDecrease(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("扣减金额必须大于0");
        }
        BigDecimal newBalance = balanceService.decreaseBalance(userId, amount);
        return Result.success(newBalance);
    }

    /**
     * 内部接口 — 增加余额（由 coupon-service 抽奖发放余额奖品调用）
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 新余额
     */
    @PostMapping("/internal/increase")
    @ApiOperation(value = "内部增加余额", notes = "由其他微服务调用，增加指定用户余额")
    public Result<BigDecimal> internalIncrease(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("增加金额必须大于0");
        }
        BigDecimal newBalance = balanceService.increaseBalance(userId, amount);
        return Result.success(newBalance);
    }
}
