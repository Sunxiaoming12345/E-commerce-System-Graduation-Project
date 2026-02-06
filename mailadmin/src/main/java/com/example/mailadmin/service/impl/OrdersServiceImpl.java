package com.example.mailadmin.service.impl;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.mailadmin.entity.Orders;
import com.example.mailadmin.mapper.AdminOrdersMapper;
import com.example.mailadmin.service.OrdersService;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    private AdminOrdersMapper adminOrdersMapper;

    @Override
    public PageResult PageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize()  );
        Page<Orders> page = adminOrdersMapper.PageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


}
