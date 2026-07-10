package com.example.orderservice.service;

import com.example.orderservice.dto.MyOrdersPageQueryDTO;
import com.example.orderservice.dto.PayDTO;
import com.example.orderservice.dto.PrePurchase;
import com.example.orderservice.entity.Orders;
import com.example.orderservice.vo.PrePurchaseVO;
import com.example.result.PageResult;
import com.github.pagehelper.Page;

public interface OrdersService {

    // 预下单
    PrePurchaseVO prepurchase(PrePurchase prePurchase);

    // 订单支付
    void pay(PayDTO payDTO);

    PageResult myOrders(MyOrdersPageQueryDTO myOrdersPageQueryDTO);

    // 创建订单
    com.example.orderservice.vo.OrderCreateResultVO createOrder(com.example.orderservice.dto.OrderCreateDTO orderCreateDTO);

    // 取消订单
    void cancelOrder(Long orderId);

    // 更新收货信息
    void updateReceiver(Long orderId, String receiverName, String receiverPhone, String shippingAddress);

    // 获取订单详情
    com.example.orderservice.vo.OrderDetailVO getOrderDetail(Long orderId);

    /** 当前用户订单统计：订单总数、总消费 */
    com.example.orderservice.vo.UserOrderStatsVO getMyOrderStats();

    // 查询订单根据订单号
  /*  Orders select(String orderNumber);*/
}
