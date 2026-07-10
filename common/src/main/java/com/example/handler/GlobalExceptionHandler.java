package com.example.handler;

import com.example.exception.BusinessException;
import com.example.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器（MVC / Servlet 栈）
 * 应用于：user-service, mailadmin, cart-service, order-service, coupon-service, refund-service, review-service
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常 — 可预期的业务错误，返回 HTTP 200 + code=0 + 错误信息
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Result handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 运行时异常 — 非预期的系统错误，返回 HTTP 500 + 通用错误信息（不泄露内部细节）
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("系统异常", e);
        return Result.error("系统异常，请稍后重试");
    }

    /**
     * 受检异常 — 未知异常，返回 HTTP 500 + 通用错误信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        log.error("未知异常", e);
        return Result.error("系统异常，请稍后重试");
    }
}
