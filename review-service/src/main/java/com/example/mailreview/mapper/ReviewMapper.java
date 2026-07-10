package com.example.mailreview.mapper;

import com.example.entity.Review;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper {

    @Insert("INSERT INTO reviews (product_id, user_id, order_id, rating, content, images, status, create_time, update_time) " +
            "VALUES (#{productId}, #{userId}, #{orderId}, #{rating}, #{content}, #{images}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "reviewId")
    void insert(Review review);

    @Select("SELECT * FROM reviews WHERE product_id = #{productId} AND status = 1 ORDER BY create_time DESC")
    List<Review> selectByProductId(@Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM reviews WHERE order_id = #{orderId} AND user_id = #{userId}")
    int countByOrderIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Integer userId);
}
