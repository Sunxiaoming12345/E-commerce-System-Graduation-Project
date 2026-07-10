package com.example.mailadmin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EditPrizeDTO {
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
}
