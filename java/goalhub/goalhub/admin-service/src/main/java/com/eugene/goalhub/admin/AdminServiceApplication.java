package com.eugene.goalhub.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台管理服务启动类。
 */
@SpringBootApplication
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
