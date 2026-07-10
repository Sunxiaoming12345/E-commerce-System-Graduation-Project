package com.example.interceptor;

import com.example.annotation.RateLimit;
import com.example.context.BaseContext;
import com.example.utils.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String PREFIX = "rate:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        RateLimit annotation = ((HandlerMethod) handler).getMethodAnnotation(RateLimit.class);
        if (annotation == null) {
            return true;
        }

        String suffix = buildSuffix(request, annotation);
        String redisKey = PREFIX + annotation.key() + ":" + suffix;

        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        if (count == 1) {
            stringRedisTemplate.expire(redisKey, annotation.seconds(), TimeUnit.SECONDS);
        }

        if (count > annotation.max()) {
            log.warn("接口限流触发：key={}, count={}, max={}, ip={}",
                    redisKey, count, annotation.max(), IpUtils.getClientIp(request));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":0,\"msg\":\"" + annotation.message() + "\"}"
            );
            return false;
        }

        return true;
    }

    private String buildSuffix(HttpServletRequest request, RateLimit annotation) {
        String keyType = annotation.keyType();
        if ("ip".equals(keyType)) {
            return IpUtils.getClientIp(request);
        }
        if ("userId".equals(keyType)) {
            Long userId = BaseContext.getCurrentId();
            return userId != null ? userId.toString() : "anonymous";
        }
        if (keyType.startsWith("param:")) {
            String paramName = keyType.substring("param:".length());
            String value = request.getParameter(paramName);
            return value != null ? value : "missing";
        }
        return "default";
    }
}
