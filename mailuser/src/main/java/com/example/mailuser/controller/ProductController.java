package com.example.mailuser.controller;

import com.example.mailuser.service.ProductService;
import com.example.mailuser.vo.CategoryVO;
import com.example.mailuser.vo.ProductVO;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 *
 * @author sunxiaoming
 * @date 2026-02-20
 */
@RestController
@RequestMapping("/user/products")
@Api(tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取推荐商品列表
     *
     * @return 推荐商品列表
     */
    @GetMapping("/recommended")
    @ApiOperation(value = "获取推荐商品列表", notes = "获取最新上架的10个商品作为推荐商品")
    public Result<List<ProductVO>> getRecommendedProducts() {
        List<ProductVO> recommendedProducts = productService.getRecommendedProducts();
        return Result.success(recommendedProducts);
    }

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    @ApiOperation(value = "获取所有商品分类", notes = "获取所有商品分类，按排序权重排序")
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = productService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 根据分类ID获取商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @GetMapping("/category/{categoryId}")
    @ApiOperation(value = "根据分类ID获取商品列表", notes = "根据选择的分类ID，显示该分类下的所有上架商品")
    public Result<List<ProductVO>> getProductsByCategoryId(@ApiParam(name = "categoryId", value = "分类ID", required = true) @PathVariable Integer categoryId) {
        List<ProductVO> products = productService.getProductsByCategoryId(categoryId);
        return Result.success(products);
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取商品详情", notes = "根据商品ID获取商品的详细信息")
    public Result<ProductVO> getProductDetail(@ApiParam(name = "id", value = "商品ID", required = true) @PathVariable Long id) {
        // 这里可以实现获取商品详情的逻辑
        // 暂时返回null，后续可以补充
        return Result.success(null);
    }
}
