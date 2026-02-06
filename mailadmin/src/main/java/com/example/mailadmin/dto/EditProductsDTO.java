package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EditProductsDTO {
    private Long id;

    private String name;

    private String description;

    private String imageUrl;

    private Integer categoryId;

    private BigDecimal price;

    private Integer stock;

    private Integer status;


}
