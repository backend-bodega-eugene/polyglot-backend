package com.eugene.goalhub.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 网关跨域配置。
 */
@Configuration
public class CorsConfig {

    /**
     * 注册全局 CORS 过滤器。
     *
     * @return CORS Web 过滤器
     */
    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // 允许跨域请求携带 Cookie 或认证信息。
        config.setAllowCredentials(true);

        // 保留你的后台管理系统端口
        config.addAllowedOrigin("http://localhost:5173");

        // 添加这些来支持前端
        config.addAllowedOrigin("http://localhost");      // 不带端口
        config.addAllowedOrigin("http://localhost:80");   // 明确指定端口 80
        config.addAllowedOrigin("http://127.0.0.1");      // IP 形式
        config.addAllowedOrigin("http://127.0.0.1:80");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // 允许所有请求头。
        config.addAllowedHeader("*");

        // 允许所有 HTTP 方法。
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // 对所有路由启用上述跨域配置。
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
