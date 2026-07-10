package com.example.orderservice.service.impl;
import com.example.exception.BusinessException;

import com.example.constant.OrderStatus;
import com.example.context.BaseContext;
import com.example.mailadmin.entity.Payments;
import com.example.orderservice.mapper.UserPaymentMapper;
import com.example.orderservice.dto.MyOrdersPageQueryDTO;
import com.example.orderservice.dto.PayDTO;
import com.example.orderservice.dto.PrePurchase;
import com.example.orderservice.entity.Orders;
import com.example.orderservice.mapper.CouponMapper;
import com.example.orderservice.mapper.ProductMapper;
import com.example.orderservice.mapper.UserOrdersMapper;
import com.example.orderservice.service.BalanceService;
import com.example.orderservice.service.CouponService;
import com.example.orderservice.service.OrdersService;
import com.example.orderservice.utils.OrderNumberUtils;
import com.example.utils.IdempotentUtil;
import com.example.orderservice.vo.PrePurchaseVO;
import com.example.orderservice.vo.UserOrderStatsVO;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.example.constant.RabbitMQConstant;
@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    private static final String ORDER_STATS_PREFIX = "user:orderStats:";
    private static final long ORDER_STATS_TTL = 10;
    private static final TimeUnit ORDER_STATS_TTL_UNIT = TimeUnit.MINUTES;
    private static final String ORDERS_PAGE_PREFIX = "user:orders:page:";
    private static final long ORDERS_PAGE_TTL = 2;
    private static final TimeUnit ORDERS_PAGE_TTL_UNIT = TimeUnit.MINUTES;
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
        com.example.orderservice.vo.ProductVO product = productMapper.getProductById(productId);
        
        // 检查商品是否存在
        if (product == null) {
            log.error("商品不存在：productId={}", productId);
            throw new BusinessException("商品不存在");
        }
        
        // 检查商品是否已下架
        if (product.getStatus() == 0) {
            log.error("商品已下架：productId={}", productId);
            throw new BusinessException("商品已下架");
        }
        
        // 检查商品库存
        if (prePurchase.getQuantity() > product.getStock()) {
            log.error("商品库存不足：productId={}, 库存={}, 需求={}", productId, product.getStock(), prePurchase.getQuantity());
            throw new BusinessException("商品库存不足");
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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserPaymentMapper userPaymentMapper;

    @Autowired
    private IdempotentUtil idempotentUtil;

    @Autowired
    private com.example.utils.CacheUtil cacheUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponMapper couponMapper;

    // 支付
    @Override
    public void pay(PayDTO payDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("支付订单：userId={}, orderId={}, orderNumber={}, paymentMethod={}",
                userId, payDTO.getOrderId(), payDTO.getOrderNumber(), payDTO.getPaymentMethod());
        
        // 设置用户ID到DTO中
        payDTO.setUserId(userId);

        // 余额支付：发 MQ 前同步校验，否则接口永远成功而异步消费端余额不足时静默失败
        if (Objects.equals(2, payDTO.getPaymentMethod())) {
            Orders order = resolveOrderForPayment(payDTO, userId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            if (!Objects.equals(order.getUserId(), userId)) {
                throw new BusinessException("订单不存在");
            }
            if (!Objects.equals(order.getOrderStatus(), OrderStatus.WAIT_PAYMENT)) {
                throw new BusinessException("订单状态不允许支付");
            }
            if (!balanceService.checkBalance(userId, order.getTotalAmount())) {
                throw new BusinessException("余额不足");
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
            invalidateOrderStats(userId);
        } catch (Exception e) {
            log.error("发送订单支付消息失败：", e);
            throw new BusinessException("支付失败");
        }
    }

    @Override
    public PageResult myOrders(MyOrdersPageQueryDTO myOrdersPageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        myOrdersPageQueryDTO.setUserId(userId);

        // 1. 尝试从 Redis 缓存读取（key 包含所有查询参数）
        String cacheKey = ORDERS_PAGE_PREFIX + userId + ":" +
                myOrdersPageQueryDTO.getPage() + ":" + myOrdersPageQueryDTO.getPageSize() +
                ":status=" + myOrdersPageQueryDTO.getOrderStatus() +
                ":name=" + (myOrdersPageQueryDTO.getName() != null ? myOrdersPageQueryDTO.getName() : "");
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("订单列表缓存命中：userId={}", userId);
            return com.alibaba.fastjson.JSON.parseObject(cached, PageResult.class);
        }

        // 2. 缓存未命中，查 DB
        log.info("订单列表缓存未命中：userId={}", userId);
        PageHelper.startPage(myOrdersPageQueryDTO.getPage(), myOrdersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.PageQuery(myOrdersPageQueryDTO);

        // 收集订单 ID，批量查 items
        java.util.List<Long> orderIds = page.getResult().stream()
                .map(Orders::getOrderId).collect(java.util.stream.Collectors.toList());
        java.util.Map<Long, java.util.List<com.example.mailadmin.entity.OrderItems>> itemsMap = new java.util.LinkedHashMap<>();
        java.util.Set<Long> productIds = new java.util.LinkedHashSet<>();
        if (!orderIds.isEmpty()) {
            java.util.List<com.example.mailadmin.entity.OrderItems> allItems = ordersMapper.selectOrderItemsByOrderIds(orderIds);
            for (com.example.mailadmin.entity.OrderItems item : allItems) {
                itemsMap.computeIfAbsent(item.getOrderId(), k -> new java.util.ArrayList<>()).add(item);
                productIds.add(item.getProductId());
            }
        }
        // 批量查商品图片
        java.util.Map<Long, String> imageMap = new java.util.HashMap<>();
        if (!productIds.isEmpty()) {
            java.util.List<com.example.orderservice.vo.ProductVO> products = productMapper.getProductsByIds(new java.util.ArrayList<>(productIds));
            for (com.example.orderservice.vo.ProductVO p : products) {
                if (p.getImageUrl() != null) imageMap.put(p.getId(), p.getImageUrl());
            }
        }

        java.util.List<com.example.orderservice.vo.OrderListVO> voList = new java.util.ArrayList<>();
        for (Orders order : page.getResult()) {
            com.example.orderservice.vo.OrderListVO vo = new com.example.orderservice.vo.OrderListVO();
            vo.setOrder(order);
            java.util.List<com.example.mailadmin.entity.OrderItems> items = itemsMap.getOrDefault(order.getOrderId(), java.util.Collections.emptyList());
            java.util.List<com.example.orderservice.vo.OrderItemVO> itemVOs = new java.util.ArrayList<>();
            for (com.example.mailadmin.entity.OrderItems item : items) {
                com.example.orderservice.vo.OrderItemVO iv = new com.example.orderservice.vo.OrderItemVO();
                iv.setItemId(item.getItemId());
                iv.setProductId(item.getProductId());
                iv.setProductName(item.getProductName());
                iv.setProductPrice(item.getProductPrice());
                iv.setQuantity(item.getQuantity());
                iv.setSubtotal(item.getSubtotal());
                iv.setImageUrl(imageMap.get(item.getProductId()));
                itemVOs.add(iv);
            }
            vo.setItems(itemVOs);
            voList.add(vo);
        }
        PageResult result = new PageResult(page.getTotal(), voList);

        // 3. 写入缓存
        stringRedisTemplate.opsForValue().set(cacheKey,
                com.alibaba.fastjson.JSON.toJSONString(result),
                ORDERS_PAGE_TTL, ORDERS_PAGE_TTL_UNIT);
        return result;
    }

    @Override
    public com.example.orderservice.vo.OrderCreateResultVO createOrder(com.example.orderservice.dto.OrderCreateDTO orderCreateDTO) {
        // 幂等性校验：消费一次性提交令牌，防止重复点击创建多笔订单
        if (!idempotentUtil.consumeToken("submit-order", orderCreateDTO.getIdempotentToken())) {
            throw new BusinessException("请勿重复提交订单");
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
            throw new BusinessException("订单创建失败");
        }

        Integer paymentTimeout = OrderStatus.PAYMENT_TIMEOUT_SECONDS;
        invalidateOrderStats(userId);
        return new com.example.orderservice.vo.OrderCreateResultVO(tempOrderNumber, paymentTimeout);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        log.info("取消订单：userId={}, orderId={}", userId, orderId);
        
        // 验证订单是否存在且属于当前用户
        Orders order = ordersMapper.getOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 只有待支付的订单可以取消
        if (order.getOrderStatus() != OrderStatus.WAIT_PAYMENT) {
            throw new BusinessException("只能取消待支付的订单");
        }
        
        // 获取订单中的商品列表
        java.util.List<com.example.mailadmin.entity.OrderItems> orderItems = ordersMapper.selectOrderItemsByOrderId(orderId);
        
        // 恢复商品库存（原子操作）
        for (com.example.mailadmin.entity.OrderItems item : orderItems) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
            log.info("恢复商品库存：productId={}, 数量={}", item.getProductId(), item.getQuantity());
            com.example.orderservice.vo.ProductVO updated = productMapper.getProductById(item.getProductId());
            if (updated != null) {
                cacheUtil.updateStockInCache(item.getProductId(), updated.getStock());
                if (updated.getStock() > 0) {
                    sendStockUpdateMessage(item.getProductId(), updated.getStock(), updated.getCategoryId());
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

        // 释放优惠券（如果使用了优惠券）
        try {
            couponService.releaseCoupon(orderId);
        } catch (Exception e) {
            log.error("释放优惠券失败：orderId={}, error={}", orderId, e.getMessage());
        }
        invalidateOrderStats(order.getUserId());
    }

    @Override
    public void updateReceiver(Long orderId, String receiverName, String receiverPhone, String shippingAddress) {
        Long userId = BaseContext.getCurrentId();
        Orders order = ordersMapper.getOrderByIdAndUserId(orderId, userId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getOrderStatus() != OrderStatus.WAIT_PAYMENT)
            throw new BusinessException("只能修改待支付订单的收货信息");
        ordersMapper.updateReceiver(orderId, receiverName, receiverPhone, shippingAddress);
    }

    @Override
    public com.example.orderservice.vo.OrderDetailVO getOrderDetail(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取订单详情：userId={}, orderId={}", userId, orderId);
        
        // 验证订单是否存在且属于当前用户
        Orders order = ordersMapper.getOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 查询订单项列表
        java.util.List<com.example.mailadmin.entity.OrderItems> orderItems = ordersMapper.selectOrderItemsByOrderId(orderId);
        
        // 创建包含图片URL的订单项VO列表
        java.util.List<com.example.orderservice.vo.OrderItemVO> orderItemVOs = new java.util.ArrayList<>();
        for (com.example.mailadmin.entity.OrderItems item : orderItems) {
            com.example.orderservice.vo.OrderItemVO orderItemVO = new com.example.orderservice.vo.OrderItemVO();
            orderItemVO.setItemId(item.getItemId());
            orderItemVO.setOrderId(item.getOrderId());
            orderItemVO.setProductId(item.getProductId());
            orderItemVO.setProductName(item.getProductName());
            orderItemVO.setProductPrice(item.getProductPrice());
            orderItemVO.setQuantity(item.getQuantity());
            orderItemVO.setSubtotal(item.getSubtotal());
            
            // 获取商品详情并添加图片URL
            com.example.orderservice.vo.ProductVO productVO = productMapper.getProductById(item.getProductId());
            if (productVO != null) {
                orderItemVO.setImageUrl(productVO.getImageUrl());
            }
            
            orderItemVOs.add(orderItemVO);
        }
        
        // 封装订单详情VO
        com.example.orderservice.vo.OrderDetailVO orderDetailVO = new com.example.orderservice.vo.OrderDetailVO();
        orderDetailVO.setOrder(order);
        orderDetailVO.setOrderItems(orderItemVOs);
        
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

        // 查询订单使用的优惠券信息
        try {
            com.example.orderservice.vo.CouponVO couponVO = couponMapper.selectCouponByOrderId(orderId);
            if (couponVO != null) {
                orderDetailVO.setCouponName(couponVO.getName());
                orderDetailVO.setCouponType(couponVO.getType());
                orderDetailVO.setCouponDiscount(couponVO.getDiscountValue());
                orderDetailVO.setCouponMinAmount(couponVO.getMinAmount());
            }
        } catch (Exception e) {
            log.error("查询订单优惠券信息失败：orderId={}", orderId, e);
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
        String key = ORDER_STATS_PREFIX + userId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return com.alibaba.fastjson.JSON.parseObject(cached, UserOrderStatsVO.class);
        }
        long total = ordersMapper.countByUserId(userId);
        BigDecimal consumption = ordersMapper.sumConsumptionByUserId(userId);
        if (consumption == null) {
            consumption = BigDecimal.ZERO;
        }
        BigDecimal balance = balanceService.getBalance(userId).getBalance();
        UserOrderStatsVO stats = new UserOrderStatsVO(total, consumption, balance);
        stringRedisTemplate.opsForValue().set(key,
                com.alibaba.fastjson.JSON.toJSONString(stats),
                ORDER_STATS_TTL, ORDER_STATS_TTL_UNIT);
        return stats;
    }

    private void invalidateOrderStats(Long userId) {
        stringRedisTemplate.delete(ORDER_STATS_PREFIX + userId);
        invalidateOrdersCache(userId);
    }

    private void invalidateOrdersCache(Long userId) {
        String pattern = ORDERS_PAGE_PREFIX + userId + ":*";
        java.util.Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
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


