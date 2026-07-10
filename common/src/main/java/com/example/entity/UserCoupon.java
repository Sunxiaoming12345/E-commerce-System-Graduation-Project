package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer userId;
    private Long couponId;
    private Integer status;
    private Long orderId;
    private LocalDateTime createTime;
    private LocalDateTime useTime;
}
