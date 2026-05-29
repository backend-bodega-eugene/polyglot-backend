package com.eugene.goalhub.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类。
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.eugene.goalhub"
        }
)
public class GatewayServiceApplication {

    /**
     * 启动网关服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
