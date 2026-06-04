package com.eugene.goalhub.admin;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台管理服务启动类。
 */
@OpenAPIDefinition(
        info = @Info(
                title = "GoalHub 后台管理服务接口文档",
                version = "1.0.0",
                description = "GoalHub 后台管理服务 OpenAPI 接口文档"
        )
)
@SpringBootApplication(
        scanBasePackages = {
                "com.eugene.goalhub"
        }
)
@MapperScan("com.eugene.goalhub.admin.mapper")
@EnableFeignClients
public class AdminServiceApplication {

    /**
     * 启动后台管理服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
