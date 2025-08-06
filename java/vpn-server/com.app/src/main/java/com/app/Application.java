package com.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.oas.annotations.EnableOpenApi;


@MapperScan(basePackages = "com.app.dao")
@SpringBootApplication(scanBasePackages = {"com.common", "com.mybatisplus", "com.app"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}