package com.example.mailuser.service;

import com.example.mailuser.entity.User;

public interface UserService {
    User getUserInfo(Long userId);
    void updateUserInfo(User user);
}