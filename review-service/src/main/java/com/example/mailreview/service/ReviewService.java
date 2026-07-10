package com.example.mailreview.service;

import com.example.entity.Review;
import com.example.mailreview.dto.ReviewSubmitDTO;

import java.util.List;

public interface ReviewService {
    void submitReview(ReviewSubmitDTO dto, Integer userId);
    List<Review> getProductReviews(Long productId);
}
