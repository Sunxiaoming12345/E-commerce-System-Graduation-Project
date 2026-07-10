package com.example.gateway.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class VerificationCodeUtils {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    // 验证码过期时间（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    // 生成6位随机验证码
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    // 存储验证码到Redis
    public void storeCode(String phone, String code) {
        String key = "verification:code:" + phone;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
    
    // 验证验证码
    public boolean verifyCode(String phone, String code) {
        String key = "verification:code:" + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        return code != null && code.equals(storedCode);
    }
    
    // 移除验证码
    public void removeCode(String phone) {
        String key = "verification:code:" + phone;
        redisTemplate.delete(key);
    }
}