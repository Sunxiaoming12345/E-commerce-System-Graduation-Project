package com.example.orderservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface RefundMapper {

    @Update("UPDATE refunds SET status = #{status}, refund_time = #{refundTime}, update_time = NOW() WHERE refund_id = #{refundId}")
    void updateRefundStatus(@Param("refundId") Long refundId, @Param("status") Integer status, @Param("refundTime") LocalDateTime refundTime);
}
