package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundApproveDTO {
    private Long refundId;
    private Boolean approved;
    private String adminRemark;
}
