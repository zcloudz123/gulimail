package com.gulimall.ware.config;

import com.gulimall.ware.interceptor.SeataInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-27-10:31
 */
@Configuration
public class MyMvcConfig extends WebMvcConfigurationSupport {
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SeataInterceptor()).addPathPatterns("/**");
    }
}
