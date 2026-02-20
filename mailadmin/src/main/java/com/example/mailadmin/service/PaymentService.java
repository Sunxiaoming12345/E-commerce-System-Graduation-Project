package com.example.mailadmin.service;

import com.example.mailadmin.dto.PaymentsPageQueryDTO;
import com.example.mailadmin.vo.PaymentStatisticsVO;
import com.example.result.PageResult;

public interface PaymentService {
    /**
     * 支付记录分页查询
     *
     * @param paymentsPageQueryDTO 支付记录查询参数
     * @return 分页结果
     */
    PageResult pageQuery(PaymentsPageQueryDTO paymentsPageQueryDTO);

    /**
     * 更新支付状态
     *
     * @param paymentId 支付ID
     * @param status 支付状态
     */
    void updateStatus(Long paymentId, Integer status);

    /**
     * 获取支付统计信息
     *
     * @return 支付统计信息
     */
    PaymentStatisticsVO getStatistics();

}
