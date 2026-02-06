package com.example.mailadmin.mapper;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.mailadmin.entity.Orders;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminOrdersMapper {
    //订单分页查询(可按照订单编号)
    Page<Orders> PageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

}
