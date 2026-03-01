package com.example.mailuser.vo;

import com.example.mailuser.entity.Orders;
import lombok.Data;

import java.util.List;

/**
 * 订单详情VO
 *
 * @author sunxiaoming
 * @date 2026-02-28
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
    private List<OrderItemVO> orderItems;
}
