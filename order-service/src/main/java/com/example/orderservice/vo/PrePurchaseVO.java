package com.example.orderservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrePurchaseVO {

    private String name;
    private BigDecimal totalAmount;
    private Integer quantity;
    private String imageUrl;
    private BigDecimal price;

}
