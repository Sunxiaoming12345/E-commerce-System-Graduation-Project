package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersPageQueryDTO {
    //订单号
    private String orderNumber;
    //用户ID
    private Long userId;
    //订单状态
    private Integer orderStatus;
    //页码
    private int page;
    //每页显示记录数
    private int pageSize;
}
