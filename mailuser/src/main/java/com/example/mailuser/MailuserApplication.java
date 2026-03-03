package com.example.mailuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.mailuser",   // 主模块
        "com.example.config",      // common 中的配置
        "com.example.handler"      // common 中的全局异常处理器
})
@MapperScan({"com.example.mailadmin.mapper","com.example.mailuser.mapper"})
public class MailuserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailuserApplication.class, args);
    }

}
