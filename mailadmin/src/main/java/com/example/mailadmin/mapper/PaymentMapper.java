package com.example.mailadmin.mapper;

import com.example.mailadmin.dto.PaymentsPageQueryDTO;
import com.example.mailadmin.entity.Payments;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper {
    /**
     * 支付记录分页查询
     *
     * @param paymentsPageQueryDTO 支付记录查询参数
     * @return 分页结果
     */
    Page<Payments> pageQuery(PaymentsPageQueryDTO paymentsPageQueryDTO);

    /**
     * 更新支付状态
     *
     * @param paymentId 支付ID
     * @param status 支付状态
     * @return 影响行数
     */
    int updateStatus(@Param("paymentId") Long paymentId, @Param("status") Integer status);

    /**
     * 获取总支付金额
     *
     * @return 总支付金额
     */
    BigDecimal getTotalAmount();

    /**
     * 获取今日支付金额
     *
     * @return 今日支付金额
     */
    BigDecimal getTodayAmount();

    /**
     * 获取本月支付金额
     *
     * @return 本月支付金额
     */
    BigDecimal getMonthAmount();

    /**
     * 获取支付方式分布（每行：paymentMethod, amount）
     */
    List<Map<String, Object>> getPaymentMethodDistribution();

    /**
     * 获取支付状态分布（每行：status, cnt）
     */
    List<Map<String, Object>> getPaymentStatusDistribution();

    /**
     * 获取总支付订单数
     *
     * @return 总支付订单数
     */
    Integer getTotalPaymentCount();

    /**
     * 获取成功支付订单数
     *
     * @return 成功支付订单数
     */
    Integer getSuccessfulPaymentCount();

    /**
     * 获取失败支付订单数
     *
     * @return 失败支付订单数
     */
    Integer getFailedPaymentCount();

}
