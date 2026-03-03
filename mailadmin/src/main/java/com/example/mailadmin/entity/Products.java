package com.example.mailadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
/**
 * 商品对象 products
 *
 * @author sunxiaoming
 * @date 2026-01-31
 */
public class Products
{
    private static final long serialVersionUID = 1L;

    /** 商品唯一ID */
    private Long id;

    /** 商品名称 */

    private String name;

    /** 商品描述 */

    private String description;

    /** 商品主图URL */

    private String imageUrl;

    /** 商品分类ID（关联categories表） */

    private Long categoryId;

    /** 商品价格 */

    private BigDecimal price;

    /** 库存数量 */

    private Integer stock;

    /** 商品状态（1:上架, 0:下架） */

    private Integer status;


    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime  updateTime;


}

