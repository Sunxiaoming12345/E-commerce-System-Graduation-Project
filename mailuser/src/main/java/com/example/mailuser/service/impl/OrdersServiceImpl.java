package com.example.mailuser.service.impl;

import com.example.constant.OrderStatus;
import com.example.context.BaseContext;
import com.example.mailadmin.entity.Products;

import com.example.mailadmin.mapper.ProductsMapper;
import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.dto.PrePurchase;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.mapper.UserOrdersMapper;
import com.example.mailuser.service.OrdersService;
import com.example.mailuser.vo.PrePurchaseVO;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private ProductsMapper productsMapper;


 // 预下单
    @Override
    public PrePurchaseVO prepurchase(PrePurchase prePurchase) {
        Products products = productsMapper.selectProductsById(BaseContext.getCurrentId());
        if(prePurchase.getQuantity()>products.getStock())
        {
            throw new RuntimeException("库存不足");
        }
        PrePurchaseVO prePurchaseVO = new PrePurchaseVO();
        prePurchaseVO.setName(products.getName());
        prePurchaseVO.setTotalAmount(products.getPrice().multiply(new BigDecimal(prePurchase.getQuantity())));
        prePurchaseVO.setQuantity(prePurchase.getQuantity());
        prePurchaseVO.setImageUrl(products.getImageUrl());
        prePurchaseVO.setPrice(products.getPrice());

        return prePurchaseVO;

    }

    // 支付
    @Override
    public void pay(PayDTO payDTO) {
        Long userId =BaseContext.getCurrentId();
        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setOrderNumber(userId.toString()+System.currentTimeMillis());
        orders.setOrderStatus(OrderStatus.WAIT_PAYMENT);
        orders.setPaymentMethod(payDTO.getPaymentMethod());
        orders.setTotalAmount(payDTO.getTotalAmount());
        orders.setShippingAddress(payDTO.getShippingAddress());
        orders.setReceiverName(payDTO.getReceiverName());
        orders.setReceiverPhone(payDTO.getReceiverPhone());
        orders.setCreateTime(LocalDateTime.now());
        ordersMapper.pay(orders);



    }

    @Override
    public PageResult myOrders(MyOrdersPageQueryDTO myOrdersPageQueryDTO) {
        myOrdersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        log.info("当前用户id:{}" , BaseContext.getCurrentId());
        PageHelper.startPage(myOrdersPageQueryDTO.getPage(),myOrdersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.PageQuery(myOrdersPageQueryDTO);
        return  new PageResult(page.getTotal(), page.getResult());
    }


}


// 查询订单根据订单号
   /* @Override
    public Orders select(String orderNumber) {
        return ordersMapper.selectByOrderNumber(orderNumber);
    }*/

