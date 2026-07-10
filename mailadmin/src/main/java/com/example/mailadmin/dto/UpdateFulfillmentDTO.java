package com.example.mailadmin.dto;

import lombok.Data;

@Data
public class UpdateFulfillmentDTO {
    private Long recordId;
    private Integer fulfillmentStatus;
    private String shippingInfo;
}
