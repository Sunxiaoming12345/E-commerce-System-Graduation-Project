package com.example.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeckillMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long seckillId;       // 秒杀商品ID
    private Long productId;       // 商品ID
    private String productName;   // 商品名称
    private Integer userId;       // 用户ID
    private BigDecimal price;     // 秒杀价格
    private Long orderId;         // 生成的订单ID（回填用）
}
