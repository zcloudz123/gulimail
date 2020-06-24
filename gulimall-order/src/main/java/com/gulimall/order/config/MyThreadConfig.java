package com.gulimall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-15:40
 */
@Configuration
public class MyThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties properties){
       return new ThreadPoolExecutor(properties.getCoreSize(),properties.getMaxSize(),properties.getKeepAliveSeconds(), TimeUnit.SECONDS,new LinkedBlockingDeque<>(10), Executors.defaultThreadFactory(),new ThreadPoolExecutor.DiscardPolicy());
    }
}
