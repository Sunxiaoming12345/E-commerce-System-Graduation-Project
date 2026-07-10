package com.example.mailcoupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponVO {
    private Long userCouponId;
    private Integer userCouponStatus;
    private Long couponId;
    private String name;
    private Integer type;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer usedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer isLottery;
    private LocalDateTime createTime;
}
