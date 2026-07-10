package com.example.mailcoupon.controller;

import com.example.constant.SeckillConstant;
import com.example.context.BaseContext;
import com.example.mailcoupon.entity.SeckillProduct;
import com.example.mailcoupon.service.SeckillService;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "秒杀")
public class SeckillController {

    private final SeckillService seckillService;

    // ===================== 用户端 =====================

    @GetMapping("/user/seckill/list")
    @ApiOperation("秒杀商品列表")
    public Result<List<SeckillProduct>> list() {
        return Result.success(seckillService.getActiveList());
    }

    @GetMapping("/user/seckill/{id}")
    @ApiOperation("秒杀商品详情")
    public Result<SeckillProduct> detail(@PathVariable Long id) {
        return Result.success(seckillService.getDetail(id));
    }

    @PostMapping("/user/seckill/{id}/buy")
    @ApiOperation("执行秒杀")
    public Result<Map<String, Object>> buy(@PathVariable Long id) {
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        if (userId == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("result", -1);
            err.put("msg", "请先登录");
            return Result.success(err);
        }

        int result = seckillService.executeSeckill(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("result", result);
        if (result == 1) {
            data.put("msg", "秒杀成功，订单处理中...");
        } else if (result == -2) {
            data.put("msg", "您已参与过该秒杀");
        } else {
            data.put("msg", "库存不足，已被抢完");
        }
        return Result.success(data);
    }

    // ===================== 管理端 =====================

    @GetMapping("/admin/seckill/list")
    @ApiOperation("管理：秒杀列表")
    public Result<List<SeckillProduct>> adminList() {
        return Result.success(seckillService.listAll());
    }

    @PostMapping("/admin/seckill/create")
    @ApiOperation("管理：创建秒杀")
    public Result<SeckillProduct> create(@RequestBody SeckillProduct product) {
        return Result.success(seckillService.create(product));
    }

    @PutMapping("/admin/seckill/{id}")
    @ApiOperation("管理：更新秒杀")
    public Result<SeckillProduct> update(@PathVariable Long id, @RequestBody SeckillProduct product) {
        return Result.success(seckillService.update(id, product));
    }

    @DeleteMapping("/admin/seckill/{id}")
    @ApiOperation("管理：删除秒杀")
    public Result<Void> delete(@PathVariable Long id) {
        seckillService.delete(id);
        return Result.success();
    }
}
