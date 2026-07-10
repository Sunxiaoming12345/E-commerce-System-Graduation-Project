package com.example.mailcoupon.controller;

import com.example.context.BaseContext;
import com.example.mailcoupon.service.LotteryService;
import com.example.mailcoupon.vo.LotteryPrizeVO;
import com.example.mailcoupon.vo.LotteryRecordVO;
import com.example.mailcoupon.vo.LotterySpinResultVO;
import com.example.result.PageResult;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user/lottery")
@Api(tags = "转盘抽奖")
public class LotteryController {

    @Autowired
    private LotteryService lotteryService;

    @GetMapping("/prizes")
    @ApiOperation("获取奖池奖品列表（用于前端渲染转盘）")
    public Result<List<LotteryPrizeVO>> prizes() {
        return Result.success(lotteryService.getActivePrizes());
    }

    @GetMapping("/cost")
    @ApiOperation("获取抽奖消耗金额")
    public Result<BigDecimal> cost() {
        return Result.success(lotteryService.getSpinCost());
    }

    @GetMapping("/status")
    @ApiOperation("检查是否可抽奖")
    public Result<Map<String, Object>> status() {
        Integer userId = BaseContext.getCurrentId().intValue();
        Map<String, Object> map = new HashMap<>();
        map.put("canSpin", lotteryService.canSpinToday(userId));
        map.put("spinCost", lotteryService.getSpinCost());
        map.put("dailyLimit", lotteryService.getDailyLimit());
        map.put("todaySpins", lotteryService.countTodaySpins(userId));
        return Result.success(map);
    }

    @PostMapping("/spin")
    @ApiOperation("转盘抽奖")
    public Result<LotterySpinResultVO> spin() {
        Integer userId = BaseContext.getCurrentId().intValue();
        LotterySpinResultVO result = lotteryService.spin(userId);
        return Result.success(result);
    }

    @GetMapping("/records")
    @ApiOperation("我的抽奖记录（分页）")
    public Result<PageResult> records(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        Integer userId = BaseContext.getCurrentId().intValue();
        return Result.success(lotteryService.getMyRecords(userId, page, pageSize));
    }
}
