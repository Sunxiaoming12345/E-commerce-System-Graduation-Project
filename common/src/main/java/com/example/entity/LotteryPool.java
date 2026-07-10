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
public class LotteryPool implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String prizeType;
    private String prizeName;
    private String prizeImage;
    private Long couponId;
    private BigDecimal balanceAmount;
    private BigDecimal probability;
    private Integer totalStock;
    private Integer remainingStock;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
