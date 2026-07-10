package com.example.mailcoupon.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillProduct {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal seckillPrice;
    private Integer stock;          // 当前库存
    private Integer originStock;    // 原始库存
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private Integer status;         // 1=未开始 2=进行中 3=已结束
    private Integer version;        // 乐观锁
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
