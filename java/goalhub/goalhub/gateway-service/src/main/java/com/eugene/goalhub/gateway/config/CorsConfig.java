package com.eugene.goalhub.gateway.config;

import org.springframework.beans.factory.annotation.Value;
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

    private final String allowedOriginPatterns;

    public CorsConfig(
            @Value("${gateway.cors.allowed-origin-patterns}") String allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

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

        for (String originPattern : allowedOriginPatterns.split(",")) {
            String trimmedPattern = originPattern.trim();
            if (!trimmedPattern.isEmpty()) {
                config.addAllowedOriginPattern(trimmedPattern);
            }
        }

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
