package com.example.mailadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example"   // 扫描所有子包（mailadmin + common 下的 config/utils/interceptor/handler 等）
})
public class MailadminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailadminApplication.class, args);
    }

}
