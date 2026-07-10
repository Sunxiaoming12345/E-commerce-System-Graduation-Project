package com.example.mailadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems {
    private Long itemId;          // 订单商品ID
    private Long orderId;         // 订单ID（外键）
    private Long productId;       // 商品ID（外键）
    private String productName;   // 商品名称
    private BigDecimal productPrice; // 商品单价
    private Integer quantity;     // 购买数量
    private BigDecimal subtotal;  // 小计金额
  //  private String imageUrl;      // 商品图片URL
}
