package com.eugene.goalhub.match;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 比赛服务启动类。
 */
@SpringBootApplication
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
