package com.example.gateway.service;

import com.example.gateway.entity.LoginLog;
import com.example.gateway.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogAsyncService {

    private final LoginLogMapper loginLogMapper;

    @Async("logExecutor")
    public void save(LoginLog loginLog) {
        try {
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("异步写入登录日志失败: {}", e.getMessage());
        }
    }
}
