package com.wxc.oj.config;

import com.wxc.oj.interceptor.LoginProtectInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置拦截器
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Autowired
    private LoginProtectInterceptor loginProtectInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器, 并配置拦截路径
        // 路径是请求路径
        registry.addInterceptor(loginProtectInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login");
    }
}