package com.example.mailadmin.controller;


import com.example.mailadmin.dto.AddProductsDTO;
import com.example.mailadmin.dto.EditProductsDTO;
import com.example.mailadmin.dto.ProductsPageQueryDTO;
import com.example.mailadmin.entity.Products;
import com.example.mailadmin.service.ProductsService;
import com.example.result.PageResult;
import com.example.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.result.Result.success;


/**
 * 商品Controller
 *
 * @author sunxiaoming
 * @date 2026-01-31
 */
@RestController
@RequestMapping("/products/products")
public class ProductsController
{
    @Autowired
    private ProductsService productsService;

    /**
     * 分页查询商品列表
     */

    @GetMapping("/page")
    public Result<PageResult> Page(@RequestBody ProductsPageQueryDTO productsPageQueryDTO)
    {

        PageResult pageResult = productsService.PageQuery(productsPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 获取商品详细信息
     */
    @GetMapping(value = "/{id}")
    public Result<Products> getInfo(@PathVariable("id") Long id)
    {
        return Result.success(productsService.selectProductsById(id));
    }

    /**
     * 新增商品
     */

    @PostMapping("/add")
    public Result add(@RequestBody AddProductsDTO addProductsDTO)
    {
        productsService.insertProducts(addProductsDTO);
        return Result.success();
    }

    /**
     * 修改商品
     */

    @PutMapping("/edit")
    public Result edit(@RequestBody EditProductsDTO editProductsDTO)
    {
        productsService.updateProducts(editProductsDTO);
        return Result.success();
    }

    /**
     * 删除商品
     */

    @DeleteMapping("/remove/{ids}")
    public Result remove(@PathVariable Long[] ids)
    {
      productsService.deleteProductsByIds(ids);
      return Result.success();
    }
}

