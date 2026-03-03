package com.example.handler;

import com.example.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理器
 * 捕获并处理运行时异常，返回友好的错误信息
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常
     * @param e 运行时异常
     * @return 错误信息
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result handleRuntimeException(RuntimeException e) {
        // 记录错误日志，但只记录错误信息，不记录完整堆栈
        log.error("运行时异常: {}", e.getMessage());
        // 返回错误信息给前端
        return Result.error(e.getMessage());
    }

    /**
     * 处理所有异常
     * @param e 异常
     * @return 错误信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        // 记录错误日志，但只记录错误信息，不记录完整堆栈
        log.error("系统异常: {}", e.getMessage());
        // 返回通用错误信息给前端
        return Result.error("系统异常，请稍后重试");
    }
}
