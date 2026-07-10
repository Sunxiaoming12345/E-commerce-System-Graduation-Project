package com.example.orderservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderStatsVO {
    /** 全部订单数（含各状态） */
    private Long totalOrders;
    /** 总消费：已付款、已发货、已完成订单金额合计 */
    private BigDecimal totalConsumption;
    /** 账户余额 */
    private BigDecimal balance;
}
