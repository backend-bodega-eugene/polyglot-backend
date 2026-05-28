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

        // 允许前端开发环境访问网关。
        //config.addAllowedOriginPattern("*");
        config.addAllowedOrigin("http://localhost:5173");

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
