package com.example.mailadmin.service;

import com.example.entity.Coupon;
import com.example.mailadmin.dto.AddCouponDTO;
import com.example.mailadmin.dto.CouponPageQueryDTO;
import com.example.mailadmin.dto.EditCouponDTO;
import com.example.result.PageResult;

public interface CouponService {
    PageResult pageQuery(CouponPageQueryDTO dto);
    Coupon getById(Long id);
    void add(AddCouponDTO dto);
    void edit(EditCouponDTO dto);
    void delete(Long id);
}
