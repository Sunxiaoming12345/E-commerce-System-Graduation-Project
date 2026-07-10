package com.example.mailadmin.controller;

import com.example.entity.Refund;
import com.example.mailadmin.dto.RefundApproveDTO;
import com.example.mailadmin.dto.RefundPageQueryDTO;
import com.example.mailadmin.service.RefundService;
import com.example.result.PageResult;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/refunds")
@Api(tags = "退款管理")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping("/page")
    @ApiOperation(value = "分页查询退款列表")
    public Result<PageResult> page(@ModelAttribute RefundPageQueryDTO dto) {
        return Result.success(refundService.pageQuery(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取退款详情")
    public Result<Refund> getById(@PathVariable Long id) {
        return Result.success(refundService.getById(id));
    }

    @PutMapping("/approve")
    @ApiOperation(value = "审批退款（通过/拒绝）")
    public Result approve(@RequestBody RefundApproveDTO dto) {
        refundService.approve(dto);
        return Result.success();
    }
}
