package com.example.mailrefund;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
    "com.example.mailrefund", "com.example.config", "com.example.interceptor",
    "com.example.annotation", "com.example.handler", "com.example.utils"
})
@MapperScan("com.example.mailrefund.mapper")
@SpringBootApplication
public class RefundServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RefundServiceApplication.class, args);
    }
}
