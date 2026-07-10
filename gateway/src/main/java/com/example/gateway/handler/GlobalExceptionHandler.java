package com.example.gateway.handler;

import com.example.exception.BusinessException;
import com.example.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 全局异常处理器（WebFlux / Gateway 栈）
 * 捕获并处理 Gateway 中的异常，返回统一 Result 格式
 *
 * @author sunxiaoming
 * @date 2026-03-11
 */
@Slf4j
@Component
public class GlobalExceptionHandler implements WebExceptionHandler, Ordered {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (ex instanceof BusinessException) {
            // 业务异常 → HTTP 200 + code=0
            log.warn("业务异常: {}", ex.getMessage());
            response.setStatusCode(HttpStatus.OK);
        } else if (ex instanceof RuntimeException) {
            // 运行时异常 → HTTP 500，不泄露内部细节
            log.error("系统异常", ex);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // 受检异常 / Error → HTTP 500
            log.error("未知异常", ex);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result errorResult;
        if (ex instanceof BusinessException) {
            errorResult = Result.error(ex.getMessage());
        } else {
            errorResult = Result.error("系统异常，请稍后重试");
        }

        DataBufferFactory bufferFactory = response.bufferFactory();
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResult);
            return response.writeWith(Mono.just(bufferFactory.wrap(bytes)));
        } catch (Exception e) {
            log.error("序列化错误响应失败", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
