package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsPageQueryDTO {

    //商品名称
    private String name;
    //分类ID
    private Long categoryId;
    //商品状态
    private Integer status;
    //页码
    private int page;

    //每页显示记录数
    private int pageSize;
}

