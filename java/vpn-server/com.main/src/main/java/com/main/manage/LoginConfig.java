package com.main.manage;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class LoginConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/backend/**")
                .excludePathPatterns("/backend/admin/login")
                .excludePathPatterns("/backend/admin/loginout")
                .excludePathPatterns("/backend/thirdpartyonly/add")
                .excludePathPatterns("/backend/recharge/recharge")
                .excludePathPatterns("/backend/admin/getVerifyCodeImg");
    }
}