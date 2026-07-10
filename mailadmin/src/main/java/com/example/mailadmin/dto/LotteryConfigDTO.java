package com.example.mailadmin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LotteryConfigDTO {
    private BigDecimal spinCost;
    private Integer dailyLimit;
}
