package com.example.mailadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payments {
    private Long paymentId;       // 支付ID
    private Long orderId;         // 订单ID（外键）
    private Integer paymentMethod; // 支付方式
    private BigDecimal amount;    // 支付金额
    private Integer status;       // 支付状态：0-未支付，1-已支付，2-支付失败
    private LocalDateTime payTime; // 支付时间

}
