package com.gulimall.gulimallseckill.config;

import com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration;
import com.gulimall.gulimallseckill.interceptor.LoginUserInterceptor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-30-22:47
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginUserInterceptor()).addPathPatterns("/**");
    }
}
