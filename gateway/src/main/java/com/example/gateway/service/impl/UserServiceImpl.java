package com.example.gateway.service.impl;

import com.example.gateway.entity.User;
import com.example.gateway.mapper.UserMapper;
import com.example.gateway.service.UserService;
import com.example.gateway.utils.JwtUtils;
import com.example.gateway.vo.UserLoginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public UserLoginInfo login(UserLoginInfo userLoginInfo) {

        User user = userMapper.login(userLoginInfo.getUsername(),userLoginInfo.getPassword());
        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",user.getId());
        claims.put("username",user.getUsername());
        claims.put("password",user.getPassword());
        String jwt =  JwtUtils.generateToken(claims);
        return new UserLoginInfo(user.getUsername(),user.getPassword(),jwt);
    }
}
