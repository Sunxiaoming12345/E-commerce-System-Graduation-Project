package com.example.mailadmin.controller;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.mailadmin.dto.UpdateOrderStatusDTO;
import com.example.mailadmin.service.OrdersService;
import com.example.mailadmin.vo.OrderDetailVO;
import com.example.mailadmin.vo.OrderStatisticsVO;
import com.example.result.PageResult;
import com.example.result.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    //订单分页查询(可根据订单状态)，GET 用查询参数
    @GetMapping("/page")
    public Result<PageResult> page(@ModelAttribute OrdersPageQueryDTO ordersPageQueryDTO){

        PageResult pageResult = ordersService.PageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);

    }

    //获取订单详情
    @GetMapping("/detail/{id}")
    public Result<OrderDetailVO> getDetail(@PathVariable("id") Long id){
        OrderDetailVO orderDetailVO = ordersService.getDetail(id);
        return Result.success(orderDetailVO);
    }

    //更新订单状态
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody UpdateOrderStatusDTO updateOrderStatusDTO){
        ordersService.updateStatus(updateOrderStatusDTO);
        return Result.success();
    }

    //获取订单统计信息
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getStatistics(){
        OrderStatisticsVO orderStatisticsVO = ordersService.getStatistics();
        return Result.success(orderStatisticsVO);
    }

}
