package com.example.mailuser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyOrdersPageQueryDTO {

    //用户id
    private Long userId;
    //订单状态
    private Integer orderStatus;
    //商品名称
    private String name ;
    //页码
    private int page;
    //每页显示记录数
    private int pageSize;
}
