package com.example.mailadmin.controller;

import com.example.entity.LotteryPool;
import com.example.entity.LotteryRecord;
import com.example.mailadmin.dto.*;
import com.example.mailadmin.service.LotteryService;
import com.example.result.PageResult;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/lottery")
@Api(tags = "抽奖管理")
public class LotteryController {

    @Autowired
    private LotteryService lotteryService;

    @GetMapping("/config")
    @ApiOperation("获取抽奖配置")
    public Result<Map<String, Object>> getConfig() {
        return Result.success(lotteryService.getConfig());
    }

    @PutMapping("/config")
    @ApiOperation("更新抽奖配置（消耗金额 + 每日限制次数）")
    public Result updateConfig(@RequestBody LotteryConfigDTO dto) {
        lotteryService.updateConfig(dto);
        return Result.success();
    }

    @GetMapping("/prizes")
    @ApiOperation("获取所有奖品")
    public Result<List<LotteryPool>> listPrizes() {
        return Result.success(lotteryService.listPrizes());
    }

    @GetMapping("/prizes/{id}")
    @ApiOperation("获取奖品详情")
    public Result<LotteryPool> getPrize(@PathVariable Long id) {
        return Result.success(lotteryService.getPrizeById(id));
    }

    @PostMapping("/prizes")
    @ApiOperation("新增奖品")
    public Result addPrize(@RequestBody AddPrizeDTO dto) {
        lotteryService.addPrize(dto);
        return Result.success();
    }

    @PutMapping("/prizes")
    @ApiOperation("编辑奖品")
    public Result editPrize(@RequestBody EditPrizeDTO dto) {
        lotteryService.editPrize(dto);
        return Result.success();
    }

    @DeleteMapping("/prizes/{id}")
    @ApiOperation("删除奖品")
    public Result deletePrize(@PathVariable Long id) {
        lotteryService.deletePrize(id);
        return Result.success();
    }

    @GetMapping("/records")
    @ApiOperation("分页查询抽奖记录")
    public Result<PageResult> pageRecords(@ModelAttribute LotteryRecordPageQueryDTO dto) {
        return Result.success(lotteryService.pageRecords(dto));
    }

    @GetMapping("/records/{id}")
    @ApiOperation("获取记录详情")
    public Result<LotteryRecord> getRecord(@PathVariable Long id) {
        return Result.success(lotteryService.getRecordById(id));
    }

    @PutMapping("/records/fulfillment")
    @ApiOperation("更新实物奖品发货状态")
    public Result updateFulfillment(@RequestBody UpdateFulfillmentDTO dto) {
        lotteryService.updateFulfillment(dto);
        return Result.success();
    }
}
