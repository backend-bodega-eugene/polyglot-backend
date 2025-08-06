package com.main;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan(basePackages = "com.main.dao")
@SpringBootApplication(scanBasePackages = {"com.common", "com.mybatisplus", "com.main"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}