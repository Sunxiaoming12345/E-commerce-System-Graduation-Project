package com.example.mailrefund.mapper;

import com.example.entity.Refund;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RefundMapper {

    @Insert("INSERT INTO refunds (order_id, user_id, amount, reason, status, create_time, update_time) " +
            "VALUES (#{orderId}, #{userId}, #{amount}, #{reason}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "refundId")
    void insert(Refund refund);

    @Select("SELECT * FROM refunds WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Refund> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM refunds WHERE refund_id = #{refundId}")
    Refund selectById(Long refundId);

    @Select("SELECT COUNT(*) FROM refunds WHERE order_id = #{orderId} AND status IN (0, 1)")
    int countPendingByOrderId(@Param("orderId") Long orderId);
}
