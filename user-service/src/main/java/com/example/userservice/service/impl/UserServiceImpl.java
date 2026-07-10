package com.example.userservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.userservice.entity.User;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String CACHE_KEY_PREFIX = "user:info:";
    private static final long CACHE_TTL = 30;
    private static final TimeUnit CACHE_TTL_UNIT = TimeUnit.MINUTES;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public User getUserInfo(Long userId) {
        log.info("获取用户信息：userId={}", userId);
        String key = CACHE_KEY_PREFIX + userId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSON.parseObject(cached, User.class);
        }
        User user = userMapper.getUserById(userId);
        if (user != null) {
            // 缓存只存必要字段，排除 password / createTime / updateTime
            java.util.Map<String, Object> cacheMap = new java.util.LinkedHashMap<>();
            cacheMap.put("id", user.getId());
            cacheMap.put("name", user.getName());
            cacheMap.put("username", user.getUsername());
            cacheMap.put("phone", user.getPhone());
            cacheMap.put("defaultReceiver", user.getDefaultReceiver());
            cacheMap.put("defaultPhone", user.getDefaultPhone());
            cacheMap.put("defaultAddress", user.getDefaultAddress());
            cacheMap.put("avatar", user.getAvatar());
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(cacheMap), CACHE_TTL, CACHE_TTL_UNIT);
        }
        return user;
    }

    @Override
    public void updateUserInfo(User user) {
        log.info("更新用户信息：userId={}, user={}", user.getId(), user);
        userMapper.updateUser(user);
        stringRedisTemplate.delete(CACHE_KEY_PREFIX + user.getId());
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.getUserById(userId);
        if (user == null) throw new com.example.exception.BusinessException("用户不存在");
        if (!user.getPassword().equals(oldPassword)) throw new com.example.exception.BusinessException("原密码错误");
        if (newPassword.length() < 6) throw new com.example.exception.BusinessException("新密码至少6位");
        userMapper.updatePassword(userId, newPassword);
    }
}
