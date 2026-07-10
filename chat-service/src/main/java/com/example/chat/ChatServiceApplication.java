package com.example.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
    "com.example.chat", "com.example.config", "com.example.interceptor",
    "com.example.annotation", "com.example.handler", "com.example.utils"
})
@SpringBootApplication
@MapperScan("com.example.chat.mapper")
public class ChatServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
