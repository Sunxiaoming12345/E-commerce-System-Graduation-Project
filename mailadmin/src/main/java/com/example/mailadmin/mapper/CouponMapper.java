package com.example.mailadmin.mapper;

import com.example.entity.Coupon;
import com.example.mailadmin.dto.CouponPageQueryDTO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper {

    Page<Coupon> pageQuery(CouponPageQueryDTO dto);

    @Select("SELECT * FROM coupons WHERE coupon_id = #{couponId}")
    Coupon selectById(Long couponId);

    @Insert("INSERT INTO coupons (name, type, discount_value, min_amount, total_count, used_count, start_time, end_time, status, create_time, update_time) " +
            "VALUES (#{name}, #{type}, #{discountValue}, #{minAmount}, #{totalCount}, 0, #{startTime}, #{endTime}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "couponId")
    void insert(Coupon coupon);

    @Update("UPDATE coupons SET name=#{name}, type=#{type}, discount_value=#{discountValue}, min_amount=#{minAmount}, " +
            "total_count=#{totalCount}, start_time=#{startTime}, end_time=#{endTime}, status=#{status}, update_time=NOW() WHERE coupon_id=#{couponId}")
    void update(Coupon coupon);

    @Delete("DELETE FROM coupons WHERE coupon_id = #{couponId}")
    void deleteById(Long couponId);
}
