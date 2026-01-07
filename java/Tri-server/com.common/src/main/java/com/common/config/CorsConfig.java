package com.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static java.util.List.of;
import static org.springframework.web.cors.CorsConfiguration.ALL;

/**
 * Cors跨域
 *
 * @author admin
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter cors() {
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",
                getConfig(new CorsConfiguration()));
        return new CorsFilter(source);
    }

    private CorsConfiguration getConfig(CorsConfiguration config) {
        config.setAllowedOriginPatterns(of("*")); //2.4.0以上 1,允许任何来源
//        config.setAllowedOrigins(of(ALL));// 1,允许任何来源
        config.addAllowedHeader(ALL); // 2,允许任何请求头
        config.addAllowedMethod(ALL); // 3,允许任何方法
        config.setAllowCredentials(true); // 4,允许任何凭证
        return config;
    }

}
