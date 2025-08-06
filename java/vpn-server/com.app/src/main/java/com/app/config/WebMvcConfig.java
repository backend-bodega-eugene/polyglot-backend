package com.app.config;

import com.app.manage.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
//        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources");
//    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new LoginInterceptor())
//                // .addPathPatterns("/app/**")
//                .excludePathPatterns("/app/member/login")
//                .excludePathPatterns("/app/member/register")
//                .excludePathPatterns("/app/member/registerv2")
//                .excludePathPatterns("/app/member/changepassword")
//                .excludePathPatterns("/app/otherapplicationinfo/**")
//                .excludePathPatterns("/app/member/visiterenter")
//                .excludePathPatterns("/app/line/query")
//                .excludePathPatterns("/app/systemconfig/**")
//                .excludePathPatterns("/app/article/**")
//                .excludePathPatterns("/app/line/**")
//                .excludePathPatterns("/swagger-ui/**");
//    }

}