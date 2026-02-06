package com.example.mailuser.controller;

import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.dto.PayDTO;
import com.example.mailuser.dto.PrePurchase;
import com.example.mailuser.entity.Orders;
import com.example.mailuser.service.OrdersService;
import com.example.mailuser.vo.PrePurchaseVO;
import com.example.result.PageResult;
import com.example.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/orders")
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
    public Result<PrePurchaseVO> prepurchase(@RequestBody PrePurchase prePurchase) {


        return Result.success(ordersService.prepurchase(prePurchase));
    }


    // 支付
    @PutMapping("/pay")
    public Result pay(@RequestBody PayDTO payDTO) {

        ordersService.pay(payDTO);

        return Result.success();
    }

    //查询我的订单
    @GetMapping("/myOrders")
    public Result<PageResult> myOrders(@RequestBody MyOrdersPageQueryDTO myOrdersPageQueryDTO) {
        PageResult pageResult = ordersService.myOrders(myOrdersPageQueryDTO);

        return Result.success(pageResult);
    }

}
