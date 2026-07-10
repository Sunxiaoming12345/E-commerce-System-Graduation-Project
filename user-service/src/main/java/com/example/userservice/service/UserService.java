package com.example.userservice.service;

import com.example.userservice.entity.User;

public interface UserService {
    User getUserInfo(Long userId);
    void updateUserInfo(User user);
    void changePassword(Long userId, String oldPassword, String newPassword);
}