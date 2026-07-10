package com.example.cartservice.controller;

import com.example.context.BaseContext;
import com.example.cartservice.dto.AddCartDTO;
import com.example.cartservice.service.ShopCartService;
import com.example.cartservice.vo.CartVO;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 *
 * @author sunxiaoming
 * @date 2026-02-10
 */
@RestController
@RequestMapping("/user/shopCart")
@Api(tags = "购物车管理")
public class ShopCartController {

    @Autowired
    private ShopCartService shopCartService;

    /**
     * 添加商品到购物车
     *
     * @param addCartDTO 添加购物车DTO
     * @return 结果
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加商品到购物车", notes = "将商品添加到购物车，已存在则更新数量")
    public Result addCart(@ApiParam(name = "addCartDTO", value = "添加购物车信息", required = true) @RequestBody AddCartDTO addCartDTO) {
        // 从BaseContext中获取用户ID
        Long userId = BaseContext.getCurrentId();
        shopCartService.addCart(userId, addCartDTO);
        return Result.success();
    }

    /**
     * 更新购物车商品数量
     *
     * @param productId 商品ID
     * @param quantity  商品数量
     * @return 结果
     */
    @PutMapping("/updateQuantity")
    @ApiOperation(value = "更新购物车商品数量", notes = "更新购物车中商品的购买数量")
    public Result updateQuantity(@ApiParam(name = "productId", value = "商品ID", required = true) @RequestParam Long productId, 
                                 @ApiParam(name = "quantity", value = "商品数量", required = true) @RequestParam Integer quantity) {
        // 从BaseContext中获取用户ID
        Long userId = BaseContext.getCurrentId();
        shopCartService.updateQuantity(userId, productId, quantity);
        return Result.success();
    }

    /**
     * 删除购物车商品
     *
     * @param productId 商品ID
     * @return 结果
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除购物车商品", notes = "从购物车中移除指定商品")
    public Result deleteCart(@ApiParam(name = "productId", value = "商品ID", required = true) @RequestParam Long productId) {
        // 从BaseContext中获取用户ID
        Long userId = BaseContext.getCurrentId();
        shopCartService.deleteCart(userId, productId);
        return Result.success();
    }

    /**
     * 清空购物车
     *
     * @return 结果
     */
    @DeleteMapping("/clear")
    @ApiOperation(value = "清空购物车", notes = "清空当前用户的所有购物车商品")
    public Result clearCart() {
        // 从BaseContext中获取用户ID
        Long userId = BaseContext.getCurrentId();
        shopCartService.clearCart(userId);
        return Result.success();
    }

    /**
     * 查询用户购物车列表
     *
     * @return 购物车列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询用户购物车列表", notes = "获取当前用户的购物车商品列表，包含商品详细信息")
    public Result<List<CartVO>> getCartList() {
        // 从BaseContext中获取用户ID
        Long userId = BaseContext.getCurrentId();
        List<CartVO> cartList = shopCartService.getCartList(userId);
        return Result.success(cartList);
    }

}
