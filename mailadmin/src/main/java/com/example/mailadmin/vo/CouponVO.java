package com.example.mailadmin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠券VO（用于订单详情中展示使用的优惠券信息）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponVO {
    private String name;
    private Integer type;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
}
