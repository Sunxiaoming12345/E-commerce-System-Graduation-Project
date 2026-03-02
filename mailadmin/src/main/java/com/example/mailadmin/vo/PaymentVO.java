package com.example.mailadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录VO
 *
 * @author sunxiaoming
 * @date 2026-03-02
 */
@Data
public class PaymentVO {
    private Long paymentId;       // 支付ID
    private Long orderId;         // 订单ID（外键）
    private String orderNumber;   // 订单编号
    private Integer paymentMethod; // 支付方式
    private BigDecimal amount;    // 支付金额
    private Integer status;       // 支付状态：0-未支付，1-已支付，2-支付失败
    private LocalDateTime payTime; // 支付时间

}