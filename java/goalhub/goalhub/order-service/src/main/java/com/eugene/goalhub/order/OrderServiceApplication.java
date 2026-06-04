package com.eugene.goalhub.order;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
        info = @Info(
                title = "GoalHub 订单服务接口文档",
                version = "1.0.0",
                description = "GoalHub 订单服务 OpenAPI 接口文档"
        )
)
@SpringBootApplication(
        scanBasePackages = {
                "com.eugene.goalhub"
        }
)
@MapperScan("com.eugene.goalhub.order.mapper")
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}