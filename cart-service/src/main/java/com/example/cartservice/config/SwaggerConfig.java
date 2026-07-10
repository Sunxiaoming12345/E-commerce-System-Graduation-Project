package com.example.cartservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Swagger配置类
 *
 * @author sunxiaoming
 * @date 2026-02-20
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    @Bean(value = "userApi")
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("Cart Service API")
                        .description("# 用户端API接口文档\n\n提供商品浏览、购物车管理等功能接口")
                        .termsOfServiceUrl("https://doc.example.com/")
                        .version("1.0.0")
                        .build())
                .groupName("用户端")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.cartservice.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}

