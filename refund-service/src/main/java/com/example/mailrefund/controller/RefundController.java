package com.example.mailrefund.controller;

import com.example.context.BaseContext;
import com.example.entity.Refund;
import com.example.mailrefund.dto.RefundSubmitDTO;
import com.example.mailrefund.service.RefundService;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/refunds")
@Api(tags = "退款管理")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping("/submit")
    @ApiOperation(value = "提交退款申请")
    public Result submit(@RequestBody RefundSubmitDTO dto) {
        Integer userId = BaseContext.getCurrentId().intValue();
        refundService.submitRefund(dto, userId);
        return Result.success();
    }

    @GetMapping("/my")
    @ApiOperation(value = "我的退款列表")
    public Result<List<Refund>> my() {
        Integer userId = BaseContext.getCurrentId().intValue();
        return Result.success(refundService.getMyRefunds(userId));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "退款详情")
    public Result<Refund> getById(@PathVariable Long id) {
        return Result.success(refundService.getById(id));
    }
}
