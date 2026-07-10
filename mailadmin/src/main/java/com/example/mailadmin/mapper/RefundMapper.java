package com.example.mailadmin.mapper;

import com.example.entity.Refund;
import com.example.mailadmin.dto.RefundPageQueryDTO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RefundMapper {

    Page<Refund> pageQuery(RefundPageQueryDTO dto);

    @Select("SELECT * FROM refunds WHERE refund_id = #{refundId}")
    Refund selectById(Long refundId);

    @Update("UPDATE refunds SET status = #{status}, admin_remark = #{adminRemark}, update_time = NOW() WHERE refund_id = #{refundId}")
    void updateStatus(@Param("refundId") Long refundId, @Param("status") Integer status, @Param("adminRemark") String adminRemark);
}
