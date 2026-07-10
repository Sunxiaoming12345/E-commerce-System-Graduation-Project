package com.example.mailreview.controller;

import com.example.context.BaseContext;
import com.example.entity.Review;
import com.example.mailreview.dto.ReviewSubmitDTO;
import com.example.mailreview.service.ReviewService;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@RequestMapping("/user/reviews")
@Api(tags = "商品评价")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/submit")
    @ApiOperation(value = "提交商品评价")
    public Result submit(
            @ApiParam(name = "reviewSubmitDTO", value = "评价信息", required = true)
            @RequestBody ReviewSubmitDTO dto) {
        Integer userId = BaseContext.getCurrentId().intValue();
        reviewService.submitReview(dto, userId);
        return Result.success();
    }

    @GetMapping("/product/{productId}")
    @ApiOperation(value = "获取商品评价列表")
    public Result<List<Review>> getProductReviews(
            @ApiParam(name = "productId", value = "商品ID", required = true)
            @PathVariable Long productId) {
        return Result.success(reviewService.getProductReviews(productId));
    }
}
