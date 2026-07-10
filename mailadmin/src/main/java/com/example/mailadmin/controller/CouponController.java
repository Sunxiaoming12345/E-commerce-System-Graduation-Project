package com.example.mailadmin.controller;

import com.example.entity.Coupon;
import com.example.mailadmin.dto.AddCouponDTO;
import com.example.mailadmin.dto.CouponPageQueryDTO;
import com.example.mailadmin.dto.EditCouponDTO;
import com.example.mailadmin.service.CouponService;
import com.example.result.PageResult;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/coupons")
@Api(tags = "优惠券管理")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/page")
    @ApiOperation(value = "分页查询优惠券")
    public Result<PageResult> page(@ModelAttribute CouponPageQueryDTO dto) {
        return Result.success(couponService.pageQuery(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取优惠券详情")
    public Result<Coupon> getById(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增优惠券")
    public Result add(@RequestBody AddCouponDTO dto) {
        couponService.add(dto);
        return Result.success();
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改优惠券")
    public Result edit(@RequestBody EditCouponDTO dto) {
        couponService.edit(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除优惠券")
    public Result delete(@PathVariable Long id) {
        couponService.delete(id);
        return Result.success();
    }
}
