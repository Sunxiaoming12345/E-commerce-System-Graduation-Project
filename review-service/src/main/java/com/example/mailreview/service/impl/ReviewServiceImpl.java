package com.example.mailreview.service.impl;
import com.example.exception.BusinessException;

import com.alibaba.fastjson.JSON;
import com.example.entity.Review;
import com.example.mailreview.dto.ReviewSubmitDTO;
import com.example.mailreview.mapper.ReviewMapper;
import com.example.mailreview.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String CACHE_KEY_PREFIX = "product:reviews:";
    private static final long CACHE_TTL = 10;
    private static final TimeUnit CACHE_TTL_UNIT = TimeUnit.MINUTES;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void submitReview(ReviewSubmitDTO dto, Integer userId) {
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException("评价内容不能为空");
        }
        if (dto.getOrderId() != null) {
            int count = reviewMapper.countByOrderIdAndUserId(dto.getOrderId(), userId);
            if (count > 0) {
                throw new BusinessException("该订单已评价");
            }
        }
        Review review = new Review();
        review.setProductId(dto.getProductId());
        review.setUserId(userId);
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        reviewMapper.insert(review);
        // 提交评价后失效该商品评价缓存
        stringRedisTemplate.delete(CACHE_KEY_PREFIX + dto.getProductId());
    }

    @Override
    public List<Review> getProductReviews(Long productId) {
        String key = CACHE_KEY_PREFIX + productId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSON.parseArray(cached, Review.class);
        }
        List<Review> list = reviewMapper.selectByProductId(productId);
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(list), CACHE_TTL, CACHE_TTL_UNIT);
        return list;
    }
}
