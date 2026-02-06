package com.example.gateway.service;

import com.example.gateway.vo.UserLoginInfo;

public interface UserService {
    UserLoginInfo login(UserLoginInfo userLoginInfo);
}
