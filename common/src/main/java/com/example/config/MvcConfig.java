package com.example.config;

import com.example.interceptor.RateLimitInterceptor;
import com.example.interceptor.UserInfoInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@Configuration
@ConditionalOnClass({DispatcherServlet.class, WebMvcConfigurer.class})
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ① 先从 header 解析 userId 放入 ThreadLocal
        registry.addInterceptor(new UserInfoInterceptor())
                .addPathPatterns("/**")
                .order(1);

        // ② 再检查请求频率（此时 BaseContext 已有 userId）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .order(2);
    }
}
