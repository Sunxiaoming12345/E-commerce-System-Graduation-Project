package com.example.mailadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付统计VO
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Data
public class PaymentStatisticsVO {
    /**
     * 总支付金额
     */
    private BigDecimal totalAmount;

    /**
     * 今日支付金额
     */
    private BigDecimal todayAmount;

    /**
     * 本月支付金额
     */
    private BigDecimal monthAmount;

    /**
     * 支付方式分布
     * key: 支付方式（0-支付宝，1-微信，2-银行卡）
     * value: 支付金额
     */
    private Map<Integer, BigDecimal> paymentMethodDistribution;

    /**
     * 支付状态分布
     * key: 支付状态（0-未支付，1-已支付，2-支付失败）
     * value: 订单数
     */
    private Map<Integer, Integer> paymentStatusDistribution;

    /**
     * 总支付订单数
     */
    private Integer totalPaymentCount;

    /**
     * 成功支付订单数
     */
    private Integer successfulPaymentCount;

    /**
     * 失败支付订单数
     */
    private Integer failedPaymentCount;

}