package com.example.mailadmin.dto;

import lombok.Data;

@Data
public class LotteryRecordPageQueryDTO {
    private String prizeType;
    private Integer fulfillmentStatus;
    private Integer page;
    private Integer pageSize;
}
