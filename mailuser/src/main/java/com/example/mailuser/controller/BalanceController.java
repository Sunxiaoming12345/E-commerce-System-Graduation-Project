package com.example.mailuser.controller;

import com.example.context.BaseContext;
import com.example.mailuser.service.BalanceService;
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
}
