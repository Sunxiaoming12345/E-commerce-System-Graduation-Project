package com.example.mailadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.mailadmin",   // 主模块
        "com.example.config"       // common 中的配置
})
public class MailadminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailadminApplication.class, args);
    }

}
