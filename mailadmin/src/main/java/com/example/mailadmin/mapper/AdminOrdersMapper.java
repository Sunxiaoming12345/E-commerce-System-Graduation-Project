package com.example.mailadmin.mapper;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.mailadmin.entity.OrderItems;
import com.example.mailadmin.entity.Orders;
import com.example.mailadmin.entity.Payments;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

import java.util.List;

@Mapper
public interface AdminOrdersMapper {
    //订单分页查询(可按照订单编号)
    Page<Orders> PageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    //根据订单ID查询订单信息
    Orders selectOrderById(Long id);

    //根据订单ID查询订单项列表
    List<OrderItems> selectOrderItemsByOrderId(Long orderId);

    //根据订单ID查询支付信息
    Payments selectPaymentByOrderId(Long orderId);

    //更新订单状态
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") Integer orderStatus);

    //获取总订单数
    Integer getTotalOrderCount();

    //获取待支付订单数
    Integer getPendingPaymentCount();

    //获取已支付订单数
    Integer getPaidCount();

    //获取已发货订单数
    Integer getShippedCount();

    //获取已完成订单数
    Integer getCompletedCount();

    //获取已取消订单数
    Integer getCancelledCount();

    //获取总销售额
    BigDecimal getTotalSales();

    //获取今日销售额
    BigDecimal getTodaySales();

    //获取本月销售额
    BigDecimal getMonthSales();

}
