package com.example.mailadmin.service.impl;

import com.example.mailadmin.dto.PaymentsPageQueryDTO;
import com.example.mailadmin.entity.Payments;
import com.example.mailadmin.mapper.PaymentMapper;
import com.example.mailadmin.service.PaymentService;
import com.example.mailadmin.vo.PaymentStatisticsVO;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public PageResult pageQuery(PaymentsPageQueryDTO paymentsPageQueryDTO) {
        PageHelper.startPage(paymentsPageQueryDTO.getPage(), paymentsPageQueryDTO.getPageSize());
        Page<com.example.mailadmin.vo.PaymentVO> page = paymentMapper.pageQuery(paymentsPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long paymentId, Integer status) {
        paymentMapper.updateStatus(paymentId, status);
    }

    @Override
    public PaymentStatisticsVO getStatistics() {
        PaymentStatisticsVO vo = new PaymentStatisticsVO();
        vo.setTotalAmount(nullToZero(paymentMapper.getTotalAmount()));
        vo.setTodayAmount(nullToZero(paymentMapper.getTodayAmount()));
        vo.setMonthAmount(nullToZero(paymentMapper.getMonthAmount()));
        vo.setTotalPaymentCount(paymentMapper.getTotalPaymentCount() != null ? paymentMapper.getTotalPaymentCount() : 0);
        vo.setSuccessfulPaymentCount(paymentMapper.getSuccessfulPaymentCount() != null ? paymentMapper.getSuccessfulPaymentCount() : 0);
        vo.setFailedPaymentCount(paymentMapper.getFailedPaymentCount() != null ? paymentMapper.getFailedPaymentCount() : 0);
        Map<Integer, BigDecimal> methodDist = new HashMap<>();
        List<Map<String, Object>> methodList = paymentMapper.getPaymentMethodDistribution();
        if (methodList != null) {
            for (Map<String, Object> row : methodList) {
                Object k = row.get("paymentMethod");
                Object v = row.get("amount");
                if (k != null) methodDist.put(((Number) k).intValue(), toBigDecimal(v));
            }
        }
        vo.setPaymentMethodDistribution(methodDist);
        Map<Integer, Integer> statusDist = new HashMap<>();
        List<Map<String, Object>> statusList = paymentMapper.getPaymentStatusDistribution();
        if (statusList != null) {
            for (Map<String, Object> row : statusList) {
                Object k = row.get("status");
                Object v = row.get("cnt");
                if (k != null) statusDist.put(((Number) k).intValue(), v != null ? ((Number) v).intValue() : 0);
            }
        }
        vo.setPaymentStatusDistribution(statusDist);
        return vo;
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        return BigDecimal.ZERO;
    }

}
