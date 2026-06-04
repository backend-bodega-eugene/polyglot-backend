package com.eugene.goalhub.user;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(
        info = @Info(
                title = "GoalHub 比用户服务接口文档",
                version = "1.0.0",
                description = "GoalHub 用户服务 OpenAPI 接口文档"
        )
)
@SpringBootApplication(
        scanBasePackages = {
                "com.eugene.goalhub"
        }
)
@MapperScan("com.eugene.goalhub.user.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}