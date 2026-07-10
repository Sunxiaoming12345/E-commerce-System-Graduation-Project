package com.example.mailcoupon.service;

import com.example.mailcoupon.vo.LotteryPrizeVO;
import com.example.mailcoupon.vo.LotteryRecordVO;
import com.example.mailcoupon.vo.LotterySpinResultVO;
import com.example.result.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface LotteryService {

    BigDecimal getSpinCost();

    List<LotteryPrizeVO> getActivePrizes();

    boolean canSpinToday(Integer userId);

    Integer getDailyLimit();

    int countTodaySpins(Integer userId);

    LotterySpinResultVO spin(Integer userId);

    PageResult getMyRecords(Integer userId, int page, int pageSize);
}
