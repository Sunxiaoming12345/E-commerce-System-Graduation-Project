package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Refund implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long refundId;
    private Long orderId;
    private Integer userId;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private String adminRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime refundTime;
}
