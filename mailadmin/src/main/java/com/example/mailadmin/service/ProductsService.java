package com.example.mailadmin.service;


import com.example.mailadmin.dto.AddProductsDTO;
import com.example.mailadmin.dto.EditProductsDTO;
import com.example.mailadmin.dto.ProductsPageQueryDTO;
import com.example.mailadmin.entity.Products;
import com.example.result.PageResult;

import java.util.List;


/**
 * 商品Service接口
 *
 * @author sunxiaoming
 * @date 2026-01-31
 */
public interface ProductsService
{

    // 分页查询商品
    PageResult PageQuery(ProductsPageQueryDTO productsPageQueryDTO);

    // 查询单个商品(具体）
    Products selectProductsById(Long id);

    // 新增商品(单个)
    void insertProducts(AddProductsDTO addProductsDTO);

    // 修改商品(单个)
    void updateProducts(EditProductsDTO editProductsDTO);

    // 删除商品
    void deleteProductsByIds(Long[] ids);
}

