package com.example.mailadmin.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计VO
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Data
public class OrderStatisticsVO {
    /**
     * 总订单数
     */
    private Integer totalOrderCount;

    /**
     * 待支付订单数
     */
    private Integer pendingPaymentCount;

    /**
     * 已支付订单数
     */
    private Integer paidCount;

    /**
     * 待发货订单数
     */
    private Integer pendingShipmentCount;

    /**
     * 已发货订单数
     */
    private Integer shippedCount;

    /**
     * 已完成订单数
     */
    private Integer completedCount;

    /**
     * 已取消订单数
     */
    private Integer cancelledCount;

    /**
     * 总销售额
     */
    private BigDecimal totalSales;

    /**
     * 今日销售额
     */
    private BigDecimal todaySales;

    /**
     * 本月销售额
     */
    private BigDecimal monthSales;

}