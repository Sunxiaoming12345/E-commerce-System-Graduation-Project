package com.example.mailcoupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@ApiModel("抽奖结果")
public class LotterySpinResultVO {
    @ApiModelProperty("奖品类型")
    private String prizeType;
    @ApiModelProperty("奖品名称")
    private String prizeName;
    @ApiModelProperty("奖品ID")
    private Long prizeId;
    @ApiModelProperty("奖品图片")
    private String prizeImage;
    @ApiModelProperty("消耗余额")
    private BigDecimal spinCost;
    @ApiModelProperty("优惠券名称(COUPON类型)")
    private String couponName;
    @ApiModelProperty("余额金额(BALANCE类型)")
    private BigDecimal balanceAmount;
    @ApiModelProperty("最新余额(扣减后实时余额)")
    private BigDecimal newBalance;
}
