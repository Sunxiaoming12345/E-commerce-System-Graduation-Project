package com.example.mailuser.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单项VO
 * 用于订单详情页面，包含商品图片URL
 */
@Data
public class OrderItemVO {
    private Long itemId;          // 订单商品ID
    private Long orderId;         // 订单ID（外键）
    private Long productId;       // 商品ID（外键）
    private String productName;   // 商品名称
    private BigDecimal productPrice; // 商品单价
    private Integer quantity;     // 购买数量
    private BigDecimal subtotal;  // 小计金额
    private String imageUrl;      // 商品图片URL
}
