package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long reviewId;
    private Long productId;
    private Integer userId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
