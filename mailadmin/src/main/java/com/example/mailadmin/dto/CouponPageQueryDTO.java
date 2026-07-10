package com.example.mailadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponPageQueryDTO {
    private String name;
    private Integer type;
    private Integer status;
    private Integer page;
    private Integer pageSize;
}
