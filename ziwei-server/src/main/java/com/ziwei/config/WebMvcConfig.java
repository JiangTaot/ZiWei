package com.ziwei.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

/**
 * Web MVC configuration - registers JWT interceptor
 *
 * @author JTWORLD
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    /**
     * 认证白名单 — 这些路径不强制要求 JWT token
     */
    private static final String[] AUTH_WHITELIST = {
            "/api/ziwei/user/login",
            "/api/ziwei/chart/calculate",  // 可选认证：OptionalJwtFilter 已登录时提取 userId 关联命盘
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/error",
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(AUTH_WHITELIST);
    }

}
