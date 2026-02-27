package com.example.mailuser.service.impl;

import com.example.mailuser.entity.User;
import com.example.mailuser.mapper.UserMapper;
import com.example.mailuser.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserInfo(Long userId) {
        log.info("获取用户信息：userId={}", userId);
        return userMapper.getUserById(userId);
    }

    @Override
    public void updateUserInfo(User user) {
        log.info("更新用户信息：userId={}, user={}", user.getId(), user);
        userMapper.updateUser(user);
    }
}
