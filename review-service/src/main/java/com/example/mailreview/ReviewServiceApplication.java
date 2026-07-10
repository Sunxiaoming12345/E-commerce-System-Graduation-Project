package com.example.mailreview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
    "com.example.mailreview", "com.example.config", "com.example.interceptor",
    "com.example.annotation", "com.example.handler", "com.example.utils"
})
@MapperScan("com.example.mailreview.mapper")
@SpringBootApplication
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
