package com.example.gateway.service;

import com.example.gateway.dto.AdminLoginInfoDTO;
import com.example.gateway.entity.Admin;
import com.example.gateway.vo.AdminLoginInfo;

public interface AdminService  {
    AdminLoginInfo login(AdminLoginInfoDTO adminLoginInfoDTO);


}
