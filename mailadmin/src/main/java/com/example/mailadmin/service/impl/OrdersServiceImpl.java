package com.example.mailadmin.service.impl;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.mailadmin.dto.UpdateOrderStatusDTO;
import com.example.mailadmin.entity.OrderItems;
import com.example.mailadmin.entity.Orders;
import com.example.mailadmin.entity.Payments;
import com.example.mailadmin.mapper.AdminOrdersMapper;
import com.example.mailadmin.service.OrdersService;
import com.example.mailadmin.vo.CouponVO;
import com.example.mailadmin.vo.OrderDetailVO;
import com.example.mailadmin.vo.OrderStatisticsVO;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    private AdminOrdersMapper adminOrdersMapper;

    @Override
    public PageResult PageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize()  );
        Page<Orders> page = adminOrdersMapper.PageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderDetailVO getDetail(Long id) {
        // 查询订单基本信息
        Orders order = adminOrdersMapper.selectOrderById(id);
        // 查询订单项列表
        List<OrderItems> orderItems = adminOrdersMapper.selectOrderItemsByOrderId(id);
        // 查询支付信息
        Payments payment = adminOrdersMapper.selectPaymentByOrderId(id);
        // 封装订单详情VO
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        orderDetailVO.setOrder(order);
        orderDetailVO.setOrderItems(orderItems);
        orderDetailVO.setPayment(payment);

        // 查询订单使用的优惠券信息
        try {
            CouponVO couponVO = adminOrdersMapper.selectCouponByOrderId(id);
            if (couponVO != null) {
                orderDetailVO.setCouponName(couponVO.getName());
                orderDetailVO.setCouponType(couponVO.getType());
                orderDetailVO.setCouponDiscount(couponVO.getDiscountValue());
                orderDetailVO.setCouponMinAmount(couponVO.getMinAmount());
            }
        } catch (Exception e) {
            // 静默处理，优惠券信息非必需
        }

        return orderDetailVO;
    }

    @Override
    public void updateStatus(UpdateOrderStatusDTO updateOrderStatusDTO) {
        adminOrdersMapper.updateOrderStatus(updateOrderStatusDTO.getOrderId(), updateOrderStatusDTO.getOrderStatus());
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        // 获取总订单数
        orderStatisticsVO.setTotalOrderCount(adminOrdersMapper.getTotalOrderCount());
        // 获取待支付订单数
        orderStatisticsVO.setPendingPaymentCount(adminOrdersMapper.getPendingPaymentCount());
        // 获取已支付订单数
        orderStatisticsVO.setPaidCount(adminOrdersMapper.getPaidCount());
        // 获取已发货订单数
        orderStatisticsVO.setShippedCount(adminOrdersMapper.getShippedCount());
        // 获取已完成订单数
        orderStatisticsVO.setCompletedCount(adminOrdersMapper.getCompletedCount());
        // 获取已取消订单数
        orderStatisticsVO.setCancelledCount(adminOrdersMapper.getCancelledCount());
        // 获取总销售额
        orderStatisticsVO.setTotalSales(adminOrdersMapper.getTotalSales());
        // 获取今日销售额
        orderStatisticsVO.setTodaySales(adminOrdersMapper.getTodaySales());
        // 获取本月销售额
        orderStatisticsVO.setMonthSales(adminOrdersMapper.getMonthSales());
        return orderStatisticsVO;
    }


}
