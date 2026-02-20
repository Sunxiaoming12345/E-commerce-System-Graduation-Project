package com.example.mailadmin.service;

import com.example.mailadmin.dto.OrdersPageQueryDTO;
import com.example.mailadmin.dto.UpdateOrderStatusDTO;
import com.example.mailadmin.vo.OrderDetailVO;
import com.example.mailadmin.vo.OrderStatisticsVO;
import com.example.result.PageResult;

public interface OrdersService {
    PageResult PageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderDetailVO getDetail(Long id);

    void updateStatus(UpdateOrderStatusDTO updateOrderStatusDTO);

    OrderStatisticsVO getStatistics();


}
