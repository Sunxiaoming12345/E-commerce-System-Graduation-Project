package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditCouponDTO {
    private Long couponId;
    private String name;
    private Integer type;
    private java.math.BigDecimal discountValue;
    private java.math.BigDecimal minAmount;
    private Integer totalCount;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
    private Integer status;
    private Integer isLottery;
}
