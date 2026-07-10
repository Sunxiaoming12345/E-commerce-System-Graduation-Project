package com.example.mailadmin.controller;

import com.example.mailadmin.dto.ReviewsPageQueryDTO;
import com.example.mailadmin.dto.UpdateReviewStatusDTO;
import com.example.mailadmin.service.ReviewService;
import com.example.result.PageResult;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/reviews")
@Api(tags = "评价管理")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/page")
    @ApiOperation(value = "分页查询评价列表")
    public Result<PageResult> page(
            @ApiParam(name = "reviewsPageQueryDTO", value = "评价分页查询参数", required = true)
            @ModelAttribute ReviewsPageQueryDTO dto) {
        return Result.success(reviewService.pageQuery(dto));
    }

    @PutMapping("/updateStatus")
    @ApiOperation(value = "更新评价状态（显示/隐藏）")
    public Result updateStatus(
            @ApiParam(name = "updateReviewStatusDTO", value = "评价状态更新参数", required = true)
            @RequestBody UpdateReviewStatusDTO dto) {
        reviewService.updateStatus(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除评价")
    public Result delete(
            @ApiParam(name = "id", value = "评价ID", required = true)
            @PathVariable Long id) {
        reviewService.delete(id);
        return Result.success();
    }
}
