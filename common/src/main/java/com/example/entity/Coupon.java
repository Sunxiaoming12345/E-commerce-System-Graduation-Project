package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long couponId;
    private String name;
    private Integer type;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer usedCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Integer isLottery;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
