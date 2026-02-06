package com.example.mailadmin.service;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.result.PageResult;

public interface OrdersService {
    PageResult PageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


}
