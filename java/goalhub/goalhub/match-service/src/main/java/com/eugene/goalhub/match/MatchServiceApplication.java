package com.eugene.goalhub.match;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 比赛服务启动类。
 */
@OpenAPIDefinition(
        info = @Info(
                title = "GoalHub 比赛服务接口文档",
                version = "1.0.0",
                description = "GoalHub 比赛服务 OpenAPI 接口文档"
        )
)
@SpringBootApplication(
        scanBasePackages = {
                "com.eugene.goalhub"
        }
)
@MapperScan("com.eugene.goalhub.match.mapper")
public class MatchServiceApplication {

    /**
     * 启动比赛服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MatchServiceApplication.class, args);
    }
}
