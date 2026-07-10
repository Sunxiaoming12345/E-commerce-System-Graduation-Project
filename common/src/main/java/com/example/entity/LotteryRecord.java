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
public class LotteryRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer userId;
    private BigDecimal spinCost;
    private String prizeType;
    private Long prizeId;
    private String prizeName;
    private Long couponId;
    private BigDecimal balanceAmount;
    private Integer fulfillmentStatus;
    private String shippingInfo;
    private LocalDateTime createTime;
}
