package com.example.mailcoupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("奖品信息")
public class LotteryPrizeVO {
    @ApiModelProperty("奖品ID")
    private Long id;
    @ApiModelProperty("奖品类型")
    private String prizeType;
    @ApiModelProperty("奖品名称")
    private String prizeName;
    @ApiModelProperty("奖品图片")
    private String prizeImage;
    @ApiModelProperty("概率")
    private BigDecimal probability;
    @ApiModelProperty("余额金额(BALANCE类型)")
    private BigDecimal balanceAmount;
    @ApiModelProperty("总库存(PHYSICAL类型)")
    private Integer totalStock;
    @ApiModelProperty("剩余库存(PHYSICAL类型)")
    private Integer remainingStock;
}
