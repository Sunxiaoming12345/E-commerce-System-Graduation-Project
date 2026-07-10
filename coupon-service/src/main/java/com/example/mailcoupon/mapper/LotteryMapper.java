package com.example.mailcoupon.mapper;

import com.example.entity.LotteryPool;
import com.example.entity.LotteryRecord;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LotteryMapper {

    @Select("SELECT spin_cost FROM lottery_config LIMIT 1")
    BigDecimal selectSpinCost();

    @Select("SELECT daily_limit FROM lottery_config LIMIT 1")
    Integer selectDailyLimit();

    @Select("SELECT * FROM lottery_pool WHERE status = 1 ORDER BY id ASC")
    List<LotteryPool> selectActivePrizes();

    @Select("SELECT * FROM lottery_pool WHERE id = #{id}")
    LotteryPool selectPrizeById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM lottery_record WHERE user_id = #{userId} AND DATE(create_time) = CURDATE()")
    int countTodaySpins(@Param("userId") Integer userId);

    @Insert("INSERT INTO lottery_record (user_id, spin_cost, prize_type, prize_id, prize_name, coupon_id, balance_amount, fulfillment_status) " +
            "VALUES (#{userId}, #{spinCost}, #{prizeType}, #{prizeId}, #{prizeName}, #{couponId}, #{balanceAmount}, #{fulfillmentStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRecord(LotteryRecord record);

    @Update("UPDATE lottery_pool SET remaining_stock = remaining_stock - 1 WHERE id = #{id} AND remaining_stock > 0")
    int decrementStock(@Param("id") Long id);

    @Select("SELECT * FROM lottery_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<LotteryRecord> selectRecordsByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM lottery_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<LotteryRecord> selectRecordsByUserIdPage(@Param("userId") Integer userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM lottery_record WHERE user_id = #{userId}")
    int countRecordsByUserId(@Param("userId") Integer userId);
}
