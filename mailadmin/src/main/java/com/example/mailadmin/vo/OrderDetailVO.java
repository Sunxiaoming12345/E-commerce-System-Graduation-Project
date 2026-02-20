package com.example.mailadmin.vo;

import com.example.mailadmin.entity.OrderItems;
import com.example.mailadmin.entity.Orders;
import com.example.mailadmin.entity.Payments;
import lombok.Data;

import java.util.List;

/**
 * 订单详情VO
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Data
public class OrderDetailVO {
    /**
     * 订单基本信息
     */
    private Orders order;

    /**
     * 订单项列表
     */
    private List<OrderItems> orderItems;

    /**
     * 支付信息
     */
    private Payments payment;
}
