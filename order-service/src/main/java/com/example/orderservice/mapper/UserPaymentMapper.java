package com.example.orderservice.mapper;

import com.example.mailadmin.entity.Payments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付记录Mapper
 *
 * @author sunxiaoming
 * @date 2026-03-02
 */
@Mapper
public interface UserPaymentMapper {
    /**
     * 插入支付记录
     *
     * @param payments 支付记录
     * @return 影响行数
     */
    int insert(Payments payments);

    /**
     * 根据订单ID查询支付记录
     *
     * @param orderId 订单ID
     * @return 支付记录
     */
    Payments getByOrderId(Long orderId);

    /**
     * 更新支付状态
     *
     * @param paymentId 支付ID
     * @param status 支付状态
     * @return 影响行数
     */
    int updateStatus(@Param("paymentId") Long paymentId, @Param("status") Integer status);

    /**
     * 更新支付金额（优惠券抵扣后更新实际支付金额）
     *
     * @param paymentId 支付ID
     * @param amount 新金额
     * @return 影响行数
     */
    int updateAmount(@Param("paymentId") Long paymentId, @Param("amount") java.math.BigDecimal amount);

}