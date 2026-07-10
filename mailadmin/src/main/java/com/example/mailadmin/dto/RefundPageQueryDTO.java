package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundPageQueryDTO {
    private Long orderId;
    private Integer userId;
    private Integer status;
    private Integer page;
    private Integer pageSize;
}
