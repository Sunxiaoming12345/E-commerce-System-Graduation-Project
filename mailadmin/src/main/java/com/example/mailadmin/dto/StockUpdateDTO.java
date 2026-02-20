package com.example.mailadmin.dto;

import lombok.Data;

/**
 * 库存更新DTO
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Data
public class StockUpdateDTO {
    /**
     * 商品ID
     */
    private Long id;

    /**
     * 库存数量
     */
    private Integer stock;
}
