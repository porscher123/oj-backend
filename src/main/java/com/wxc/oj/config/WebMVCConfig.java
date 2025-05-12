package com.wxc.oj.config;

import cn.hutool.json.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.wxc.oj.interceptor.LoginProtectInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置拦截器
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Autowired
    private LoginProtectInterceptor loginProtectInterceptor;


    /**
     * 除了用户登陆和注册
     * 其余功能都要校验先token
     * 用户登录和用户注册api方向，其它接口必须校验token，在拦截器中校验token
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器, 并配置拦截路径
        // 路径是请求路径
        registry.addInterceptor(loginProtectInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/swagger-ui/**")
                .excludePathPatterns("/v3/**");
    }


}