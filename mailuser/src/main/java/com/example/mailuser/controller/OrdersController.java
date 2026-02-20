package com.example.mailuser.controller;

import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.dto.PrePurchase;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.service.OrdersService;
import com.example.mailuser.vo.PrePurchaseVO;
import com.example.result.PageResult;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/orders")
@Api(tags = "订单管理")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

  /*  // 查询订单根据订单号
    @GetMapping("/select/{orderNumber}")
    public Result<Orders> select(@PathVariable String orderNumber) {

        return Result.success(ordersService.select(orderNumber));
    }
*/


    // 预下单
    @PostMapping("/prepurchase")
    @ApiOperation(value = "预下单", notes = "创建订单前的预下单操作，获取商品信息和总价")
    public Result<PrePurchaseVO> prepurchase(@ApiParam(name = "prePurchase", value = "预下单信息", required = true) @RequestBody PrePurchase prePurchase) {


        return Result.success(ordersService.prepurchase(prePurchase));
    }


    // 支付
    @PutMapping("/pay")
    @ApiOperation(value = "支付订单", notes = "支付订单，更新订单状态为已支付")
    public Result pay(@ApiParam(name = "payDTO", value = "支付信息", required = true) @RequestBody PayDTO payDTO) {

        ordersService.pay(payDTO);

        return Result.success();
    }

    //查询我的订单
    @GetMapping("/myOrders")
    @ApiOperation(value = "查询我的订单", notes = "分页查询当前用户的订单列表")
    public Result<PageResult> myOrders(@ApiParam(name = "myOrdersPageQueryDTO", value = "订单分页查询参数", required = true) @RequestBody MyOrdersPageQueryDTO myOrdersPageQueryDTO) {
        PageResult pageResult = ordersService.myOrders(myOrdersPageQueryDTO);

        return Result.success(pageResult);
    }

}
