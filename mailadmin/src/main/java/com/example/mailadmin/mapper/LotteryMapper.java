package com.example.mailadmin.mapper;

import com.example.entity.LotteryPool;
import com.example.entity.LotteryRecord;
import com.example.mailadmin.dto.LotteryRecordPageQueryDTO;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LotteryMapper {

    @Select("SELECT spin_cost FROM lottery_config LIMIT 1")
    BigDecimal selectSpinCost();

    @Select("SELECT daily_limit FROM lottery_config LIMIT 1")
    Integer selectDailyLimit();

    @Update("UPDATE lottery_config SET spin_cost = #{cost}, daily_limit = #{dailyLimit}")
    int updateConfig(@Param("cost") BigDecimal cost, @Param("dailyLimit") Integer dailyLimit);

    @Select("SELECT * FROM lottery_pool ORDER BY id ASC")
    List<LotteryPool> selectAllPrizes();

    @Select("SELECT * FROM lottery_pool WHERE id = #{id}")
    LotteryPool selectPrizeById(@Param("id") Long id);

    @Insert("INSERT INTO lottery_pool (prize_type, prize_name, prize_image, coupon_id, balance_amount, probability, total_stock, remaining_stock, status) " +
            "VALUES (#{prizeType}, #{prizeName}, #{prizeImage}, #{couponId}, #{balanceAmount}, #{probability}, #{totalStock}, #{remainingStock}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPrize(LotteryPool prize);

    int updatePrize(LotteryPool prize);

    @Delete("DELETE FROM lottery_pool WHERE id = #{id}")
    int deletePrize(@Param("id") Long id);

    List<LotteryRecord> pageRecords(LotteryRecordPageQueryDTO dto);

    @Select("SELECT * FROM lottery_record WHERE id = #{id}")
    LotteryRecord selectRecordById(@Param("id") Long id);

    @Update("UPDATE lottery_record SET fulfillment_status = #{status}, shipping_info = #{shippingInfo} WHERE id = #{id}")
    int updateFulfillment(@Param("id") Long id, @Param("status") Integer status, @Param("shippingInfo") String shippingInfo);
}
