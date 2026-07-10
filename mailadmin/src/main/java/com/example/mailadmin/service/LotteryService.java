package com.example.mailadmin.service;

import com.example.entity.LotteryPool;
import com.example.entity.LotteryRecord;
import com.example.mailadmin.dto.*;
import com.example.result.PageResult;

import java.util.List;
import java.util.Map;

public interface LotteryService {
    Map<String, Object> getConfig();
    void updateConfig(LotteryConfigDTO dto);
    List<LotteryPool> listPrizes();
    LotteryPool getPrizeById(Long id);
    void addPrize(AddPrizeDTO dto);
    void editPrize(EditPrizeDTO dto);
    void deletePrize(Long id);
    PageResult pageRecords(LotteryRecordPageQueryDTO dto);
    LotteryRecord getRecordById(Long id);
    void updateFulfillment(UpdateFulfillmentDTO dto);
}
