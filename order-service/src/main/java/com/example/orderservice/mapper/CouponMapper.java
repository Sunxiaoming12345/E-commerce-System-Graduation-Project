package com.example.orderservice.mapper;

import com.example.entity.Coupon;
import com.example.entity.UserCoupon;
import com.example.orderservice.vo.CouponVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper {

    @Select("SELECT * FROM coupons WHERE coupon_id = #{couponId}")
    Coupon selectById(@Param("couponId") Long couponId);

    @Select("SELECT * FROM user_coupons WHERE id = #{id}")
    UserCoupon selectUserCouponById(@Param("id") Long id);

    @Update("UPDATE user_coupons SET status = #{status}, order_id = #{orderId}, use_time = NOW() WHERE id = #{id} AND status = 0")
    int updateUserCouponStatus(@Param("id") Long id, @Param("status") Integer status, @Param("orderId") Long orderId);

    @Select("SELECT c.name, c.type, c.discount_value, c.min_amount FROM user_coupons uc JOIN coupons c ON uc.coupon_id = c.coupon_id WHERE uc.order_id = #{orderId}")
    CouponVO selectCouponByOrderId(@Param("orderId") Long orderId);

    @Update("UPDATE user_coupons SET status = 0, order_id = NULL, use_time = NULL WHERE order_id = #{orderId} AND status = 1")
    int releaseUserCoupon(@Param("orderId") Long orderId);
}
