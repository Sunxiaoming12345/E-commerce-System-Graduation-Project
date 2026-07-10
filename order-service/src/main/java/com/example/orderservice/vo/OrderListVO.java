package com.example.orderservice.vo;

import com.example.orderservice.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderListVO {
    private Orders order;
    private List<OrderItemVO> items;
}
