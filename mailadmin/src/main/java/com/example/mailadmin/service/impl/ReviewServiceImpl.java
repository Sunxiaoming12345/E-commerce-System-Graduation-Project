package com.example.mailadmin.service.impl;
import com.example.exception.BusinessException;

import com.example.entity.Review;
import com.example.mailadmin.dto.ReviewsPageQueryDTO;
import com.example.mailadmin.dto.UpdateReviewStatusDTO;
import com.example.mailadmin.mapper.ReviewMapper;
import com.example.mailadmin.service.ReviewService;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult pageQuery(ReviewsPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Review> page = reviewMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(UpdateReviewStatusDTO dto) {
        Review review = reviewMapper.selectById(dto.getReviewId());
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.updateStatus(dto.getReviewId(), dto.getStatus());
        // 失效该商品评价缓存
        stringRedisTemplate.delete("product:reviews:" + review.getProductId());
    }

    @Override
    public void delete(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.deleteById(reviewId);
        // 失效该商品评价缓存
        stringRedisTemplate.delete("product:reviews:" + review.getProductId());
    }
}
