package com.example.mailuser.service;

import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.dto.PrePurchase;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.vo.PrePurchaseVO;
import com.example.result.PageResult;
import com.github.pagehelper.Page;

public interface OrdersService {

    // 预下单
    PrePurchaseVO prepurchase(PrePurchase prePurchase);

    // 订单支付
    void pay(PayDTO payDTO);

    PageResult myOrders(MyOrdersPageQueryDTO myOrdersPageQueryDTO);

    // 创建订单
    com.example.mailuser.vo.OrderCreateResultVO createOrder(com.example.mailuser.dto.OrderCreateDTO orderCreateDTO);

    // 取消订单
    void cancelOrder(Long orderId);

    // 获取订单详情
    com.example.mailuser.vo.OrderDetailVO getOrderDetail(Long orderId);

    // 查询订单根据订单号
  /*  Orders select(String orderNumber);*/
}
