package com.example.mailuser.service.impl;

import com.example.constant.OrderStatus;
import com.example.context.BaseContext;
import com.example.mailadmin.entity.Payments;
import com.example.mailuser.mapper.UserPaymentMapper;
import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.dto.PrePurchase;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.mapper.ProductMapper;
import com.example.mailuser.mapper.UserOrdersMapper;
import com.example.mailuser.service.BalanceService;
import com.example.mailuser.mapper.CouponMapper;
import com.example.mailuser.service.CouponService;
import com.example.mailuser.service.OrdersService;
import com.example.mailuser.utils.OrderNumberUtils;
import com.example.utils.IdempotentUtil;
import com.example.mailuser.vo.PrePurchaseVO;
import com.example.mailuser.vo.UserOrderStatsVO;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
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
        // 获取商品ID
        Long productId = prePurchase.getProductId();
        
        // 从数据库中获取商品信息
        com.example.mailuser.vo.ProductVO product = productMapper.getProductById(productId);
        
        // 检查商品是否存在
        if (product == null) {
            log.error("商品不存在：productId={}", productId);
            throw new RuntimeException("商品不存在");
        }
        
        // 检查商品是否已下架
        if (product.getStatus() == 0) {
            log.error("商品已下架：productId={}", productId);
            throw new RuntimeException("商品已下架");
        }
        
        // 检查商品库存
        if (prePurchase.getQuantity() > product.getStock()) {
            log.error("商品库存不足：productId={}, 库存={}, 需求={}", productId, product.getStock(), prePurchase.getQuantity());
            throw new RuntimeException("商品库存不足");
        }
        
        PrePurchaseVO prePurchaseVO = new PrePurchaseVO();
        prePurchaseVO.setName(product.getName());
        prePurchaseVO.setTotalAmount(product.getPrice().multiply(new BigDecimal(prePurchase.getQuantity())));
        prePurchaseVO.setQuantity(prePurchase.getQuantity());
        prePurchaseVO.setImageUrl(product.getImageUrl());
        prePurchaseVO.setPrice(product.getPrice());

        return prePurchaseVO;

    }

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    @Autowired
    private IdempotentUtil idempotentUtil;

    // 支付
    @Override
    public void pay(PayDTO payDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("支付订单：userId={}, orderId={}, orderNumber={}, paymentMethod={}",
                userId, payDTO.getOrderId(), payDTO.getOrderNumber(), payDTO.getPaymentMethod());
        
        // 设置用户ID到DTO中
        payDTO.setUserId(userId);

        // 余额支付：发 MQ 前同步校验，否则接口永远成功而异步消费端余额不足时静默失败
        // 异步下单可能存在延迟，重试 10 次（共约 5 秒）
        if (Objects.equals(2, payDTO.getPaymentMethod())) {
            Orders order = null;
            for (int i = 0; i < 10; i++) {
                order = resolveOrderForPayment(payDTO, userId);
                if (order != null) break;
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
            if (order == null) {
                throw new RuntimeException("订单处理中，请稍后支付");
            }
            if (!Objects.equals(order.getUserId(), userId)) {
                throw new RuntimeException("订单不存在");
            }
            if (!Objects.equals(order.getOrderStatus(), OrderStatus.WAIT_PAYMENT)) {
                throw new RuntimeException("订单状态不允许支付");
            }
            if (!balanceService.checkBalance(userId, order.getTotalAmount())) {
                throw new RuntimeException("余额不足");
            }
        }
        
        // 发送订单支付消息到RabbitMQ
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.ORDER_PAY_EXCHANGE_NAME,
                    RabbitMQConstant.ORDER_PAY_ROUTING_KEY,
                    payDTO
            );
            log.info("发送订单支付消息：userId={}, orderId={}, orderNumber={}", userId, payDTO.getOrderId(), payDTO.getOrderNumber());
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
    public com.example.mailuser.vo.OrderCreateResultVO createOrder(com.example.mailuser.dto.OrderCreateDTO orderCreateDTO) {
        // 幂等性校验：消费一次性提交令牌，防止重复点击创建多笔订单
        if (!idempotentUtil.consumeToken("submit-order", orderCreateDTO.getIdempotentToken())) {
            throw new RuntimeException("请勿重复提交订单");
        }

        Long userId = BaseContext.getCurrentId();
        log.info("创建订单：userId={}, orderData={}", userId, orderCreateDTO);

        orderCreateDTO.setUserId(userId);

        String tempOrderNumber = OrderNumberUtils.generate(userId);
        orderCreateDTO.setOrderNumber(tempOrderNumber);

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

        Integer paymentTimeout = OrderStatus.PAYMENT_TIMEOUT_SECONDS;
        return new com.example.mailuser.vo.OrderCreateResultVO(tempOrderNumber, paymentTimeout);
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
                    productMapper.restoreStock(item.getProductId(), newStock);
                    log.info("恢复商品库存：productId={}, 原库存={}, 增加数量={}, 新库存={}", item.getProductId(), oldStock, item.getQuantity(), newStock);
                // 只有当库存从0到有时才发送消息
                if (oldStock == 0 && newStock > 0) {
                    sendStockUpdateMessage(item.getProductId(), newStock, productVO.getCategoryId());
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

        // 释放已使用的优惠券
        couponService.releaseCoupon(orderId);
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

        // 查询订单使用的优惠券
        com.example.mailuser.vo.CouponVO couponVO = couponMapper.selectCouponByOrderId(orderId);
        if (couponVO != null) {
            orderDetailVO.setCouponName(couponVO.getName());
            orderDetailVO.setCouponType(couponVO.getType());
            orderDetailVO.setCouponDiscount(couponVO.getDiscountValue());
        }

        // 查询支付记录
        Payments payment = userPaymentMapper.getByOrderId(orderId);
        orderDetailVO.setPayment(payment);

        // 计算支付剩余时间（秒）
        if (order.getOrderStatus() == OrderStatus.WAIT_PAYMENT) {
            // 支付超时时间：订单创建时间 + 支付超时时间常量
            LocalDateTime paymentTimeoutTime = order.getCreateTime().plusSeconds(OrderStatus.PAYMENT_TIMEOUT_SECONDS);
            // 当前时间
            LocalDateTime currentTime = LocalDateTime.now();
            // 计算剩余时间（秒）
            long remainingSeconds = java.time.Duration.between(currentTime, paymentTimeoutTime).getSeconds();
            // 如果剩余时间小于0，设置为0
            orderDetailVO.setPaymentRemainingTime((int) Math.max(0, remainingSeconds));
        } else {
            // 非待支付状态，支付剩余时间为0
            orderDetailVO.setPaymentRemainingTime(0);
        }
        
        return orderDetailVO;
    }

    /** 与 OrderPayMessageConsumer 一致的订单解析逻辑 */
    private Orders resolveOrderForPayment(PayDTO payDTO, Long userId) {
        Orders order = null;
        String orderNumber = payDTO.getOrderNumber();
        if (orderNumber != null && !orderNumber.isBlank()) {
            order = ordersMapper.selectByOrderNumber(orderNumber.trim());
        } else if (payDTO.getOrderId() != null) {
            order = ordersMapper.getOrderByIdAndUserId(payDTO.getOrderId(), userId);
            if (order == null) {
                order = ordersMapper.selectByOrderNumber(payDTO.getOrderId().toString());
            }
        }
        return order;
    }

    @Override
    public UserOrderStatsVO getMyOrderStats() {
        Long userId = BaseContext.getCurrentId();
        long total = ordersMapper.countByUserId(userId);
        BigDecimal consumption = ordersMapper.sumConsumptionByUserId(userId);
        if (consumption == null) {
            consumption = BigDecimal.ZERO;
        }
        return new UserOrderStatsVO(total, consumption);
    }

    // 发送库存更新消息到RabbitMQ
    private void sendStockUpdateMessage(Long productId, Integer stock, Long categoryId) {
        try {
            // 构建包含商品ID、库存和分类ID的消息
            String message = productId + "," + stock + "," + categoryId;
            rabbitTemplate.convertAndSend(RabbitMQConstant.STOCK_EXCHANGE_NAME, RabbitMQConstant.STOCK_ROUTING_KEY, message);
            log.info("发送库存更新消息：productId={}, stock={}, categoryId={}", productId, stock, categoryId);
        } catch (Exception e) {
            log.error("发送库存更新消息失败：", e);
        }
    }

}


// 查询订单根据订单号
   /* @Override
    public Orders select(String orderNumber) {
        return ordersMapper.selectByOrderNumber(orderNumber);
    }*/

