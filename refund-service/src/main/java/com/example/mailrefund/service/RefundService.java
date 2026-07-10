package com.example.mailrefund.service;

import com.example.entity.Refund;
import com.example.mailrefund.dto.RefundSubmitDTO;

import java.util.List;

public interface RefundService {
    void submitRefund(RefundSubmitDTO dto, Integer userId);
    List<Refund> getMyRefunds(Integer userId);
    Refund getById(Long refundId);
}
