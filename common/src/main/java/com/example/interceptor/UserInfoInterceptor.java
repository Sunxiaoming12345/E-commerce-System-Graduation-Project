package com.example.interceptor;

import com.example.context.BaseContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取登录用户信息
        String userInfo = request.getHeader("user-info");
        if (StringUtils.isNotBlank(userInfo)) {
            BaseContext.setCurrentId(Long.valueOf(userInfo));
        }
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.remove();
    }
}
