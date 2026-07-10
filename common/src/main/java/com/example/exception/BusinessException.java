package com.example.exception;

/**
 * 业务异常 — 用于表示可预期的业务逻辑错误（如账号密码错误、库存不足等）。
 * 与系统异常（如数据库连接失败、Redis 超时）区分开来。
 * <p>
 * 在 Gateway (WebFlux) 中，BusinessException 返回 HTTP 200 + code=0；
 * 在微服务 (MVC) 中，通过 {@code @ControllerAdvice} 返回 HTTP 200 + code=0。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
