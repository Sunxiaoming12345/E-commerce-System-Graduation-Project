package com.example.mailadmin.controller;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.mailadmin.service.OrdersService;
import com.example.result.PageResult;
import com.example.result.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    //订单分页查询(可根据订单状态)
    @GetMapping ("/page")
    public Result<PageResult> page(@RequestBody OrdersPageQueryDTO ordersPageQueryDTO){

        PageResult pageResult = ordersService.PageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);

    }

}
