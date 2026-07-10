package com.example.mailadmin.service;

import com.example.entity.Refund;
import com.example.mailadmin.dto.RefundApproveDTO;
import com.example.mailadmin.dto.RefundPageQueryDTO;
import com.example.result.PageResult;

public interface RefundService {
    PageResult pageQuery(RefundPageQueryDTO dto);
    Refund getById(Long id);
    void approve(RefundApproveDTO dto);
}
