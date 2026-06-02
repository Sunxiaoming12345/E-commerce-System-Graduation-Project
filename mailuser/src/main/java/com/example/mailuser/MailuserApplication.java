package com.example.mailuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example"   // 扫描所有子包（mailuser/mailadmin + common 下的 config/utils/interceptor/handler 等）
})
@MapperScan({"com.example.mailadmin.mapper","com.example.mailuser.mapper"})
public class MailuserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailuserApplication.class, args);
    }

}
