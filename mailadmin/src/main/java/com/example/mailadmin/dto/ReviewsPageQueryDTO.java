package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewsPageQueryDTO {
    private Long productId;
    private Integer userId;
    private Integer rating;
    private Integer status;
    private Integer page;
    private Integer pageSize;
}
