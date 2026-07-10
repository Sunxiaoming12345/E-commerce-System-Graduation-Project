package com.example.mailrefund.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundSubmitDTO {
    private Long orderId;
    private BigDecimal amount;
    private String reason;
}
