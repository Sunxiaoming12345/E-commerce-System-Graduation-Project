package com.example.gateway.filter;

import com.example.gateway.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if(isExclude(request.getPath().toString()))
        {
            return chain.filter(exchange);
        }
        String token=null ;
        List<String> headers = request.getHeaders().get("authorization");
        if(headers!=null&&!headers.isEmpty())
        {
            token = headers.get(0);
        }
        Long userId=null;
        try {
            userId=jwtUtils.parseToken(token).get("id", Long.class);
        } catch (Exception e) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return  response.setComplete();
        }
        log.info("用户id:{}",userId);
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        // 定义需要放行的路径
        return path.contains("/login") || path.contains("/register");
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
