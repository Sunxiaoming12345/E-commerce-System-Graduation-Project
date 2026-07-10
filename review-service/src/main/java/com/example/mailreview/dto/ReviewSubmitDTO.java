package com.example.mailreview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSubmitDTO {
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
}
