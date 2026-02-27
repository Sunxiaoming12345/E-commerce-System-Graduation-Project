package com.example.mailuser.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    private Long orderId;           // 订单ID
    private Long userId;            // 用户ID
    private String orderNumber;     // 订单编号
    private Integer orderStatus;    // 订单状态：0-待付款，1-已付款，2-已发货，3-已完成，4-已取消
    private Integer paymentMethod;  // 支付方式：0-支付宝，1-微信，2-余额
    private BigDecimal totalAmount; // 订单总金额
    private String shippingAddress; // 收货地址
    private String receiverName;    // 收货人姓名
    private String receiverPhone;   // 收货人电话
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
    private LocalDateTime payTime;     // 支付时间
    private LocalDateTime shipTime;    // 发货时间
    private LocalDateTime completeTime;// 完成时间
}
