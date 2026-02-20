package com.example.mailuser.controller;

import com.example.context.BaseContext;
import com.example.mailuser.dto.AddCartDTO;
import com.example.mailuser.service.ShopCartService;
import com.example.mailuser.vo.CartVO;
import com.example.result.Result;
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
    public Result addCart(@RequestBody AddCartDTO addCartDTO) {
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
    public Result updateQuantity(@RequestParam Long productId, @RequestParam Integer quantity) {
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
    public Result deleteCart(@RequestParam Long productId) {
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
    public Result<List<CartVO>> getCartList() {
        // 从BaseContext中获取用户ID
        Long userId = BaseContext.getCurrentId();
        List<CartVO> cartList = shopCartService.getCartList(userId);
        return Result.success(cartList);
    }

}
