package com.example.mailadmin.dto;

import lombok.Data;

/**
 * 订单状态更新DTO
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Data
public class UpdateOrderStatusDTO {
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单状态
     * 0-待付款，1-已付款，2-待发货，3-已发货，4-已完成，5-已取消
     */
    private Integer orderStatus;

    /**
     * 备注
     */
    private String remark;
}
