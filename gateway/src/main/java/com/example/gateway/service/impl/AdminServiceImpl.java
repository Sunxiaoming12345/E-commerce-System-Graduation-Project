package com.example.gateway.service.impl;

import com.example.gateway.dto.AdminLoginInfoDTO;
import com.example.gateway.entity.Admin;
import com.example.gateway.mapper.AdminMapper;
import com.example.gateway.service.AdminService;
import com.example.gateway.utils.JwtUtils;
import com.example.gateway.vo.AdminLoginInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private  AdminMapper adminMapper;
    @Override
    public AdminLoginInfo login(AdminLoginInfoDTO adminLoginInfoDTO) {

        Admin admin = adminMapper.adminLogin(adminLoginInfoDTO.getUsername(),adminLoginInfoDTO.getPassword());
    if (admin == null) {
        throw new RuntimeException("账号或密码错误");
    }
        Map<String,Object> claims = new HashMap<>();
         claims.put("id",admin.getId());
         claims.put("username",admin.getUsername());
         claims.put("password",admin.getPassword());
        String jwt =  JwtUtils.generateToken(claims);
        return new AdminLoginInfo(admin.getUsername(),admin.getPassword(),jwt);
    }
}
