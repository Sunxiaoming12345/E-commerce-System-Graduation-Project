package com.example.mailadmin.service;

import com.example.mailadmin.dto.ReviewsPageQueryDTO;
import com.example.mailadmin.dto.UpdateReviewStatusDTO;
import com.example.result.PageResult;

public interface ReviewService {
    PageResult pageQuery(ReviewsPageQueryDTO dto);
    void updateStatus(UpdateReviewStatusDTO dto);
    void delete(Long reviewId);
}
