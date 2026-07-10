package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCouponDTO {
    private String name;
    private Integer type;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer isLottery;
}
