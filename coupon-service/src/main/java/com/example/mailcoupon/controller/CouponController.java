package com.example.mailcoupon.controller;

import com.example.context.BaseContext;
import com.example.entity.Coupon;
import com.example.mailcoupon.service.CouponService;
import com.example.mailcoupon.vo.CouponVO;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user/coupons")
@Api(tags = "优惠券")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/available")
    @ApiOperation(value = "获取可领取的优惠券")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.getAvailableCoupons());
    }

    @PostMapping("/claim/{couponId}")
    @ApiOperation(value = "领取优惠券")
    public Result claim(@PathVariable Long couponId) {
        Integer userId = BaseContext.getCurrentId().intValue();
        couponService.claimCoupon(userId, couponId);
        return Result.success();
    }

    @GetMapping("/my")
    @ApiOperation(value = "我的优惠券")
    public Result<List<CouponVO>> my(@RequestParam(required = false) Integer status) {
        Integer userId = BaseContext.getCurrentId().intValue();
        return Result.success(couponService.getMyCoupons(userId, status));
    }

    @PostMapping("/preview")
    @ApiOperation(value = "预览优惠券抵扣金额")
    public Result<BigDecimal> preview(@RequestBody Map<String, Object> body) {
        Long userCouponId = Long.valueOf(body.get("userCouponId").toString());
        BigDecimal orderAmount = new BigDecimal(body.get("orderAmount").toString());
        return Result.success(couponService.calculateDiscount(userCouponId, orderAmount));
    }
}
