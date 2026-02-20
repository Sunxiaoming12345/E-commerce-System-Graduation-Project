package com.example.mailuser.dto;

import lombok.Data;

/**
 * 添加购物车DTO
 *
 * @author sunxiaoming
 * @date 2026-02-10
 */
@Data
public class AddCartDTO {
    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品数量
     */
    private Integer quantity;

}
