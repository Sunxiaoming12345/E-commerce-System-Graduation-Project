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

    @Autowired
    private BalanceService balanceService;

    // 支付
    @Override
    public void pay(PayDTO payDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("支付订单：userId={}, orderId={}, paymentMethod={}", userId, payDTO.getOrderId(), payDTO.getPaymentMethod());
        
        // 验证订单是否存在且属于当前用户
        Orders order = ordersMapper.getOrderByIdAndUserId(payDTO.getOrderId(), userId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 只有待支付的订单可以支付
        if (order.getOrderStatus() != OrderStatus.WAIT_PAYMENT) {
            throw new RuntimeException("只能支付待支付的订单");
        }
        
        // 如果是余额支付，检查余额是否足够
        if (payDTO.getPaymentMethod() == 2) {
            boolean isEnough = balanceService.checkBalance(userId, order.getTotalAmount());
            if (!isEnough) {
                throw new RuntimeException("余额不足");
            }
            // 扣除余额
            balanceService.decreaseBalance(userId, order.getTotalAmount());
            log.info("余额支付成功：userId={}, amount={}", userId, order.getTotalAmount());
        }
        
        // 更新订单状态为已支付，并更新支付方式
        order.setOrderStatus(OrderStatus.PAID);
        order.setPaymentMethod(payDTO.getPaymentMethod());
        order.setPayTime(LocalDateTime.now());
        
        // 这里可以添加其他支付方式的逻辑，比如调用第三方支付接口等
        
        // 更新订单支付信息
        ordersMapper.updateOrderForPayment(payDTO.getOrderId(), OrderStatus.PAID, payDTO.getPaymentMethod());
    }

    @Override
    public PageResult myOrders(MyOrdersPageQueryDTO myOrdersPageQueryDTO) {
        myOrdersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        log.info("当前用户id:{}" , BaseContext.getCurrentId());
        PageHelper.startPage(myOrdersPageQueryDTO.getPage(),myOrdersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.PageQuery(myOrdersPageQueryDTO);
        return  new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void createOrder(com.example.mailuser.dto.OrderCreateDTO orderCreateDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("创建订单：userId={}, orderData={}", userId, orderCreateDTO);
        
        // 创建订单记录
        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setOrderNumber(userId.toString() + System.currentTimeMillis());
        orders.setOrderStatus(com.example.constant.OrderStatus.WAIT_PAYMENT);
        orders.setPaymentMethod(orderCreateDTO.getPaymentMethod());
        orders.setTotalAmount(orderCreateDTO.getTotalAmount());
        orders.setShippingAddress(orderCreateDTO.getShippingAddress());
        orders.setReceiverName(orderCreateDTO.getReceiverName());
        orders.setReceiverPhone(orderCreateDTO.getReceiverPhone());
        orders.setCreateTime(java.time.LocalDateTime.now());
        
        // 保存订单
        ordersMapper.pay(orders);
        
        // 从购物车中移除已结算的商品
        for (com.example.mailuser.dto.OrderCreateDTO.OrderItemDTO item : orderCreateDTO.getItems()) {
            // 这里可以调用购物车服务移除商品
            // 暂时注释，后续可以实现
            log.info("从购物车移除商品：productId={}, quantity={}", item.getProductId(), item.getQuantity());
        }
    }

    @Override
    public void cancelOrder(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        log.info("取消订单：userId={}, orderId={}", userId, orderId);
        
        // 验证订单是否存在且属于当前用户
        Orders order = ordersMapper.getOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 只有待支付的订单可以取消
        if (order.getOrderStatus() != OrderStatus.WAIT_PAYMENT) {
            throw new RuntimeException("只能取消待支付的订单");
        }
        
        // 更新订单状态为已取消
        ordersMapper.updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    @Override
    public Orders getOrderDetail(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取订单详情：userId={}, orderId={}", userId, orderId);
        
        // 验证订单是否存在且属于当前用户
        Orders order = ordersMapper.getOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        return order;
    }

}


// 查询订单根据订单号
   /* @Override
    public Orders select(String orderNumber) {
        return ordersMapper.selectByOrderNumber(orderNumber);
    }*/

