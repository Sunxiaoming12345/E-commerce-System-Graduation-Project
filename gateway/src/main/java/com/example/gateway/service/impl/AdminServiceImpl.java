package com.example.gateway.service.impl;

import com.example.gateway.dto.AdminLoginInfoDTO;
import com.example.gateway.entity.Admin;
import com.example.gateway.mapper.AdminMapper;
import com.example.gateway.service.AdminService;
import com.example.gateway.utils.JwtUtils;
import com.example.gateway.vo.AdminLoginInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOGIN_FAIL_PREFIX = "login:fail:admin:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 15;

    @Override
    public AdminLoginInfo login(AdminLoginInfoDTO adminLoginInfoDTO) {
        String failKey = LOGIN_FAIL_PREFIX + adminLoginInfoDTO.getUsername();

        String failCount = stringRedisTemplate.opsForValue().get(failKey);
        if (failCount != null && Integer.parseInt(failCount) >= MAX_FAIL_COUNT) {
            throw new RuntimeException("账号已被锁定，请" + LOCK_MINUTES + "分钟后再试");
        }

        Admin admin = adminMapper.adminLogin(adminLoginInfoDTO.getUsername(), adminLoginInfoDTO.getPassword());
        if (admin == null) {
            Long count = stringRedisTemplate.opsForValue().increment(failKey);
            if (count == 1) {
                stringRedisTemplate.expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);
            }
            int remaining = MAX_FAIL_COUNT - count.intValue();
            throw new RuntimeException("账号或密码错误，还剩" + remaining + "次尝试机会");
        }

        stringRedisTemplate.delete(failKey);
        Map<String,Object> claims = new HashMap<>();
        claims.put("id", admin.getId());
        claims.put("username", admin.getUsername());
        String jwt = JwtUtils.generateToken(claims);
        return new AdminLoginInfo(admin.getUsername(), null, jwt);
    }
}
