package com.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-16-12:11
 */
@Configuration
public class MyRedissonConfig {
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.159.129:6379");
        return Redisson.create(config);
    }
}
