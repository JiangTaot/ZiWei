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
     * Whitelist paths that do not require authentication
     */
    private static final String[] AUTH_WHITELIST = {
            "/api/ziwei/user/login",
            "/api/ziwei/chart/calculate",  // public: calculate charts
            "/api/ziwei/chart/*",          // public: view any chart by ID (GET)
            "/api/ziwei/chart/my-list",    // public: list charts (returns empty if no userId)
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
