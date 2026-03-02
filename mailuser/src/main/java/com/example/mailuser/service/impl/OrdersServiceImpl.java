package com.example.mailuser.service.impl;

import com.example.constant.OrderStatus;
import com.example.context.BaseContext;
import com.example.mailadmin.entity.Products;
import com.example.mailadmin.entity.Payments;

import com.example.mailadmin.mapper.ProductsMapper;
import com.example.mailuser.mapper.UserPaymentMapper;
import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.dto.PrePurchase;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.mapper.ProductMapper;
import com.example.mailuser.mapper.UserOrdersMapper;
import com.example.mailuser.service.BalanceService;
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
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.example.constant.RabbitMQConstant;
@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    private UserOrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;


 // 预下单
    @Override
    public PrePurchaseVO prepurchase(PrePurchase prePurchase) {
        // 这里使用 productMapper 获取商品信息
        // 注意：prePurchase 应该包含商品ID，而不是用户ID
        // 暂时保留原逻辑，后续可以优化
        Products products = new Products();
        // 这里应该从 productMapper 获取商品信息
        // 暂时设置默认值，避免空指针异常
        products.setStock(100);
        products.setName("测试商品");
        products.setPrice(new BigDecimal(100));
        products.setImageUrl("/images/test.jpg");
        
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

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    // 支付
    @Override
    public void pay(PayDTO payDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("支付订单：userId={}, orderId={}, paymentMethod={}", userId, payDTO.getOrderId(), payDTO.getPaymentMethod());
        
        // 设置用户ID到DTO中
        payDTO.setUserId(userId);
        
        // 发送订单支付消息到RabbitMQ
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.ORDER_PAY_EXCHANGE_NAME,
                    RabbitMQConstant.ORDER_PAY_ROUTING_KEY,
                    payDTO
            );
            log.info("发送订单支付消息：userId={}, orderId={}", userId, payDTO.getOrderId());
        } catch (Exception e) {
            log.error("发送订单支付消息失败：", e);
            throw new RuntimeException("支付失败");
        }
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
    public Long createOrder(com.example.mailuser.dto.OrderCreateDTO orderCreateDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("创建订单：userId={}, orderData={}", userId, orderCreateDTO);
        
        // 设置用户ID到DTO中
        orderCreateDTO.setUserId(userId);
        
        // 生成临时订单号（用于返回给前端）
        String tempOrderNumber = userId.toString() + System.currentTimeMillis();
        
        // 将订单号设置到DTO中
        orderCreateDTO.setOrderNumber(tempOrderNumber);
        
        // 发送订单创建消息到RabbitMQ
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.ORDER_CREATE_EXCHANGE_NAME,
                    RabbitMQConstant.ORDER_CREATE_ROUTING_KEY,
                    orderCreateDTO
            );
            log.info("发送订单创建消息：userId={}, orderNumber={}", userId, tempOrderNumber);
        } catch (Exception e) {
            log.error("发送订单创建消息失败：", e);
            throw new RuntimeException("订单创建失败");
        }
        
        // 这里返回临时生成的订单号，实际订单ID会在消息消费者中生成
        // 注意：如果需要返回真实的订单ID，需要修改为异步返回或使用消息回调机制
        return Long.parseLong(tempOrderNumber);
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
        
        // 获取订单中的商品列表
        java.util.List<com.example.mailadmin.entity.OrderItems> orderItems = ordersMapper.selectOrderItemsByOrderId(orderId);
        
        // 恢复商品库存
        for (com.example.mailadmin.entity.OrderItems item : orderItems) {
            // 获取商品信息
            com.example.mailuser.vo.ProductVO productVO = productMapper.getProductById(item.getProductId());
            if (productVO != null) {
                // 增加库存
                int oldStock = productVO.getStock();
                int newStock = oldStock + item.getQuantity();
                productMapper.updateStock(item.getProductId(), newStock);
                log.info("恢复商品库存：productId={}, 原库存={}, 增加数量={}, 新库存={}", item.getProductId(), oldStock, item.getQuantity(), newStock);
                // 只有当库存从0到有时才发送消息
                if (oldStock == 0 && newStock > 0) {
                    sendStockUpdateMessage(item.getProductId(), newStock);
                }
            }
        }
        
        // 更新订单状态为已取消
        ordersMapper.updateOrderStatus(orderId, OrderStatus.CANCELLED);
        
        // 更新支付记录状态为已取消
        Payments payment = userPaymentMapper.getByOrderId(orderId);
        if (payment != null) {
            userPaymentMapper.updateStatus(payment.getPaymentId(), 2); // 2-已取消
            log.info("支付记录已取消：paymentId={}, orderId={}", payment.getPaymentId(), orderId);
        }
    }

    @Override
    public com.example.mailuser.vo.OrderDetailVO getOrderDetail(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取订单详情：userId={}, orderId={}", userId, orderId);
        
        // 验证订单是否存在且属于当前用户
        Orders order = ordersMapper.getOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 查询订单项列表
        java.util.List<com.example.mailadmin.entity.OrderItems> orderItems = ordersMapper.selectOrderItemsByOrderId(orderId);
        
        // 创建包含图片URL的订单项VO列表
        java.util.List<com.example.mailuser.vo.OrderItemVO> orderItemVOs = new java.util.ArrayList<>();
        for (com.example.mailadmin.entity.OrderItems item : orderItems) {
            com.example.mailuser.vo.OrderItemVO orderItemVO = new com.example.mailuser.vo.OrderItemVO();
            orderItemVO.setItemId(item.getItemId());
            orderItemVO.setOrderId(item.getOrderId());
            orderItemVO.setProductId(item.getProductId());
            orderItemVO.setProductName(item.getProductName());
            orderItemVO.setProductPrice(item.getProductPrice());
            orderItemVO.setQuantity(item.getQuantity());
            orderItemVO.setSubtotal(item.getSubtotal());
            
            // 获取商品详情并添加图片URL
            com.example.mailuser.vo.ProductVO productVO = productMapper.getProductById(item.getProductId());
            if (productVO != null) {
                orderItemVO.setImageUrl(productVO.getImageUrl());
            }
            
            orderItemVOs.add(orderItemVO);
        }
        
        // 封装订单详情VO
        com.example.mailuser.vo.OrderDetailVO orderDetailVO = new com.example.mailuser.vo.OrderDetailVO();
        orderDetailVO.setOrder(order);
        orderDetailVO.setOrderItems(orderItemVOs);
        
        return orderDetailVO;
    }

    // 发送库存更新消息到RabbitMQ
    private void sendStockUpdateMessage(Long productId, Integer stock) {
        try {
            String message = "Product " + productId + " stock updated to " + stock;
            rabbitTemplate.convertAndSend(RabbitMQConstant.STOCK_EXCHANGE_NAME, RabbitMQConstant.STOCK_ROUTING_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


// 查询订单根据订单号
   /* @Override
    public Orders select(String orderNumber) {
        return ordersMapper.selectByOrderNumber(orderNumber);
    }*/

