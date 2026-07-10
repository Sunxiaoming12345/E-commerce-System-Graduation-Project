package com.example.mailadmin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.constant.RabbitMQConstant;
import com.example.entity.LotteryPool;
import com.example.entity.LotteryRecord;
import com.example.exception.BusinessException;
import com.example.mailadmin.dto.*;
import com.example.mailadmin.mapper.LotteryMapper;
import com.example.mailadmin.service.LotteryService;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LotteryServiceImpl implements LotteryService {

    @Autowired
    private LotteryMapper lotteryMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("spinCost", lotteryMapper.selectSpinCost());
        map.put("dailyLimit", lotteryMapper.selectDailyLimit());
        return map;
    }

    @Override
    public void updateConfig(LotteryConfigDTO dto) {
        if (dto.getSpinCost() == null || dto.getSpinCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("抽奖消耗必须大于0");
        }
        Integer dailyLimit = dto.getDailyLimit();
        if (dailyLimit == null || dailyLimit < 0) {
            dailyLimit = 1;
        }
        lotteryMapper.updateConfig(dto.getSpinCost(), dailyLimit);
        // 通知 coupon-service 刷新抽奖配置缓存
        try {
            JSONObject msg = new JSONObject();
            msg.put("timestamp", System.currentTimeMillis());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.COUPON_CACHE_EXCHANGE_NAME,
                    RabbitMQConstant.COUPON_CACHE_ROUTING_KEY,
                    msg.toJSONString());
        } catch (Exception e) {
            log.warn("发布缓存刷新消息失败", e);
        }
        log.info("更新抽奖配置：spinCost={}, dailyLimit={}", dto.getSpinCost(), dailyLimit);
    }

    @Override
    public List<LotteryPool> listPrizes() {
        return lotteryMapper.selectAllPrizes();
    }

    @Override
    public LotteryPool getPrizeById(Long id) {
        LotteryPool prize = lotteryMapper.selectPrizeById(id);
        if (prize == null) {
            throw new BusinessException("奖品不存在");
        }
        return prize;
    }

    @Override
    public void addPrize(AddPrizeDTO dto) {
        LotteryPool prize = new LotteryPool();
        BeanUtils.copyProperties(dto, prize);
        if (prize.getStatus() == null) prize.setStatus(1);
        // 实物库存初始化
        if ("PHYSICAL".equals(dto.getPrizeType()) && dto.getTotalStock() != null) {
            prize.setRemainingStock(dto.getTotalStock());
        }
        lotteryMapper.insertPrize(prize);
        publishCacheRefresh();
        log.info("新增抽奖奖品：{}", prize.getPrizeName());
    }

    @Override
    public void editPrize(EditPrizeDTO dto) {
        LotteryPool prize = new LotteryPool();
        BeanUtils.copyProperties(dto, prize);
        lotteryMapper.updatePrize(prize);
        publishCacheRefresh();
        log.info("编辑抽奖奖品：id={}", dto.getId());
    }

    @Override
    public void deletePrize(Long id) {
        lotteryMapper.deletePrize(id);
        publishCacheRefresh();
        log.info("删除抽奖奖品：id={}", id);
    }

    private void publishCacheRefresh() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("timestamp", System.currentTimeMillis());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.COUPON_CACHE_EXCHANGE_NAME,
                    RabbitMQConstant.COUPON_CACHE_ROUTING_KEY,
                    msg.toJSONString());
        } catch (Exception e) {
            log.warn("发布缓存刷新消息失败", e);
        }
    }

    @Override
    public PageResult pageRecords(LotteryRecordPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<LotteryRecord> page = (Page<LotteryRecord>) lotteryMapper.pageRecords(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public LotteryRecord getRecordById(Long id) {
        LotteryRecord record = lotteryMapper.selectRecordById(id);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }
        return record;
    }

    @Override
    public void updateFulfillment(UpdateFulfillmentDTO dto) {
        lotteryMapper.updateFulfillment(dto.getRecordId(), dto.getFulfillmentStatus(), dto.getShippingInfo());
        log.info("更新实物发货状态：recordId={}, status={}", dto.getRecordId(), dto.getFulfillmentStatus());
    }
}
