package com.example.mailcoupon.mapper;

import com.example.entity.Coupon;
import com.example.entity.UserCoupon;
import com.example.mailcoupon.vo.CouponVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CouponMapper {

    @Select("SELECT * FROM coupons WHERE status = 1 AND is_lottery = 0 AND used_count < total_count AND start_time <= NOW() AND end_time >= NOW()")
    List<Coupon> selectAvailable();

    @Select("SELECT * FROM coupons WHERE coupon_id = #{couponId}")
    Coupon selectById(@Param("couponId") Long couponId);

    @Insert("INSERT INTO user_coupons (user_id, coupon_id, status, create_time) VALUES (#{userId}, #{couponId}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUserCoupon(UserCoupon userCoupon);

    List<CouponVO> selectUserCoupons(@Param("userId") Integer userId, @Param("status") Integer status);

    @Select("SELECT * FROM user_coupons WHERE id = #{id}")
    UserCoupon selectUserCouponById(@Param("id") Long id);

    @Update("UPDATE user_coupons SET status = #{status}, order_id = #{orderId}, use_time = NOW() WHERE id = #{id} AND status = 0")
    int updateUserCouponStatus(@Param("id") Long id, @Param("status") Integer status, @Param("orderId") Long orderId);

    @Select("SELECT * FROM user_coupons WHERE order_id = #{orderId}")
    UserCoupon selectUserCouponByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT c.name, c.type, c.discount_value FROM user_coupons uc JOIN coupons c ON uc.coupon_id = c.coupon_id WHERE uc.order_id = #{orderId}")
    CouponVO selectCouponByOrderId(@Param("orderId") Long orderId);

    @Update("UPDATE user_coupons SET status = 0, order_id = NULL, use_time = NULL WHERE order_id = #{orderId} AND status = 1")
    int releaseUserCoupon(@Param("orderId") Long orderId);

    @Update("UPDATE coupons SET used_count = used_count + 1 WHERE coupon_id = #{couponId}")
    void incrementUsedCount(@Param("couponId") Long couponId);

    @Select("SELECT COUNT(*) FROM user_coupons WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countUserCoupon(@Param("userId") Integer userId, @Param("couponId") Long couponId);

    @Update("UPDATE user_coupons uc JOIN coupons c ON uc.coupon_id = c.coupon_id SET uc.status = 2 WHERE uc.user_id = #{userId} AND uc.status = 0 AND c.end_time < NOW()")
    int expireUserCoupons(@Param("userId") Integer userId);
}
