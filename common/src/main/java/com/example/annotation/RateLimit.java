package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解，基于 Redis INCR + EXPIRE 实现固定窗口计数
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** Redis key 前缀 */
    String key();

    /** 时间窗口内的最大请求次数 */
    int max();

    /** 时间窗口长度（秒） */
    int seconds();

    /**
     * key 拼接方式：
     * <ul>
     *   <li>{@code ip} — 客户端 IP</li>
     *   <li>{@code userId} — 已登录用户 ID（从 BaseContext 取）</li>
     *   <li>{@code param:xxx} — 从 request parameter 取（如 param:phone）</li>
     * </ul>
     */
    String keyType() default "ip";

    /** 超过限制时返回的提示信息 */
    String message() default "操作过于频繁，请稍后再试";
}
