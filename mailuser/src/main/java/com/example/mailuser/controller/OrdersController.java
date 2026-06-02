package com.example.mailuser.controller;

import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.dto.OrderCreateDTO;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.dto.PrePurchase;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.service.OrdersService;
import com.example.mailuser.vo.PrePurchaseVO;
import com.example.mailuser.vo.UserOrderStatsVO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.utils.IdempotentUtil;
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

    @Autowired
    private IdempotentUtil idempotentUtil;

    @GetMapping("/submit-token")
    @ApiOperation(value = "获取提交令牌", notes = "获取一次性幂等令牌，用于防止重复提交订单")
    public Result<String> getSubmitToken() {
        return Result.success(idempotentUtil.reserveToken("submit-order", 300));
    }

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
    @PostMapping("/pay")
    @ApiOperation(value = "支付订单", notes = "支付订单，更新订单状态为已支付")
    public Result pay(@ApiParam(name = "payDTO", value = "支付信息", required = true) @RequestBody PayDTO payDTO) {

        ordersService.pay(payDTO);

        return Result.success();
    }

    //查询我的订单
    @GetMapping("/myOrders")
    @ApiOperation(value = "查询我的订单", notes = "分页查询当前用户的订单列表")
    public Result<PageResult> myOrders(@ApiParam(name = "myOrdersPageQueryDTO", value = "订单分页查询参数", required = true) @ModelAttribute MyOrdersPageQueryDTO myOrdersPageQueryDTO) {
        PageResult pageResult = ordersService.myOrders(myOrdersPageQueryDTO);

        return Result.success(pageResult);
    }

    @GetMapping("/stats")
    @ApiOperation(value = "我的订单统计", notes = "订单总数与总消费（已付款及以上状态计入消费）")
    public Result<UserOrderStatsVO> myOrderStats() {
        return Result.success(ordersService.getMyOrderStats());
    }

    // 创建订单
    @PostMapping("/create")
    @ApiOperation(value = "创建订单", notes = "根据购物车商品创建新订单")
    public Result<com.example.mailuser.vo.OrderCreateResultVO> createOrder(@ApiParam(name = "orderData", value = "订单创建信息", required = true) @RequestBody OrderCreateDTO orderData) {
        com.example.mailuser.vo.OrderCreateResultVO result = ordersService.createOrder(orderData);
        return Result.success(result);
    }

    // 取消订单
    @PutMapping("/cancel/{id}")
    @ApiOperation(value = "取消订单", notes = "取消待支付的订单")
    public Result cancelOrder(@ApiParam(name = "id", value = "订单ID", required = true) @PathVariable Long id) {
        ordersService.cancelOrder(id);
        return Result.success();
    }

    // 获取订单详情
    @GetMapping("/{id}")
    @ApiOperation(value = "获取订单详情", notes = "根据订单ID获取订单详情，包含商品信息")
    public Result<com.example.mailuser.vo.OrderDetailVO> getOrderDetail(@ApiParam(name = "id", value = "订单ID", required = true) @PathVariable Long id) {
        com.example.mailuser.vo.OrderDetailVO orderDetailVO = ordersService.getOrderDetail(id);
        return Result.success(orderDetailVO);
    }

}
