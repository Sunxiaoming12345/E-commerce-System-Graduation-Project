package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewStatusDTO {
    private Long reviewId;
    private Integer status;
}
