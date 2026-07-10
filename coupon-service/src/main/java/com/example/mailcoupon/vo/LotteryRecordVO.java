package com.example.mailcoupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("抽奖记录")
public class LotteryRecordVO {
    @ApiModelProperty("记录ID")
    private Long id;
    @ApiModelProperty("奖品类型")
    private String prizeType;
    @ApiModelProperty("奖品名称")
    private String prizeName;
    @ApiModelProperty("奖品图片")
    private String prizeImage;
    @ApiModelProperty("优惠券名称")
    private String couponName;
    @ApiModelProperty("余额金额")
    private BigDecimal balanceAmount;
    @ApiModelProperty("消耗余额")
    private BigDecimal spinCost;
    @ApiModelProperty("发货状态")
    private Integer fulfillmentStatus;
    @ApiModelProperty("物流信息")
    private String shippingInfo;
    @ApiModelProperty("中奖时间")
    private LocalDateTime createTime;
}
