package com.example.mailadmin.mapper;

import com.example.entity.Review;
import com.example.mailadmin.dto.ReviewsPageQueryDTO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReviewMapper {

    Page<Review> pageQuery(ReviewsPageQueryDTO dto);

    @Select("SELECT * FROM reviews WHERE review_id = #{reviewId}")
    Review selectById(Long reviewId);

    @Update("UPDATE reviews SET status = #{status}, update_time = NOW() WHERE review_id = #{reviewId}")
    void updateStatus(@Param("reviewId") Long reviewId, @Param("status") Integer status);

    @Delete("DELETE FROM reviews WHERE review_id = #{reviewId}")
    void deleteById(Long reviewId);
}
