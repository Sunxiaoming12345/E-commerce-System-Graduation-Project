package com.example.gateway.service;

import com.example.gateway.dto.UserLoginInfoDTO;
import com.example.gateway.vo.UserLoginInfo;
import com.example.gateway.entity.User;
import com.example.gateway.dto.UserRegisterDTO;

public interface UserService {
    UserLoginInfo login(UserLoginInfoDTO userLoginInfoDTO, String clientIp);
    User register(UserRegisterDTO userRegisterDTO);
    void sendVerificationCode(String phone);
}
