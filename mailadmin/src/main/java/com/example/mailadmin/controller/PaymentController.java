package com.example.mailadmin.controller;

import com.example.mailadmin.dto.PaymentsPageQueryDTO;
import com.example.mailadmin.service.PaymentService;
import com.example.mailadmin.vo.PaymentStatisticsVO;
import com.example.result.PageResult;
import com.example.result.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // 支付记录分页查询，GET 用查询参数
    @GetMapping("/page")
    public Result<PageResult> page(@ModelAttribute PaymentsPageQueryDTO paymentsPageQueryDTO) {
        PageResult pageResult = paymentService.pageQuery(paymentsPageQueryDTO);
        return Result.success(pageResult);
    }

    // 更新支付状态
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestParam Long paymentId, @RequestParam Integer status) {
        paymentService.updateStatus(paymentId, status);
        return Result.success();
    }

    // 获取支付统计信息
    @GetMapping("/statistics")
    public Result<PaymentStatisticsVO> getStatistics() {
        PaymentStatisticsVO paymentStatisticsVO = paymentService.getStatistics();
        return Result.success(paymentStatisticsVO);
    }

}
