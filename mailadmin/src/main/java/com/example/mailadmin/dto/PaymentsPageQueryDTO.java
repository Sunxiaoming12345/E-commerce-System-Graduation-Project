package com.example.mailadmin.dto;

import lombok.Data;

/**
 * 支付记录查询DTO
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Data
public class PaymentsPageQueryDTO {
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付状态
     * 0-未支付，1-已支付，2-支付失败
     */
    private Integer status;

    /**
     * 支付方式
     * 0-支付宝，1-微信，2-银行卡
     */
    private Integer paymentMethod;

    /**
     * 页码
     */
    private int page;

    /**
     * 每页显示记录数
     */
    private int pageSize;
}
