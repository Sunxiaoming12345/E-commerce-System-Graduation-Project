package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayDTO {
   /** 数据库订单主键，与订单列表/详情一致时使用 */
   private Long orderId;
   /** 业务订单号；创建订单后立即支付时传此字段（可含字母） */
   private String orderNumber;
   //用户ID
   private Long userId;
   //商品名称
   private String name;
   //商品总价
   private BigDecimal totalAmount;
   //商品数量
   private Integer quantity;
   //商品图片
   private String imageUrl;
   //商品单价
   private BigDecimal price;
   //支付方式
   private Integer paymentMethod;
   //收货地址
   private String shippingAddress;
   //收货人名称
   private String receiverName;
   //收货人电话
   private String receiverPhone;
   //创建时间
   private String createTime;
}
