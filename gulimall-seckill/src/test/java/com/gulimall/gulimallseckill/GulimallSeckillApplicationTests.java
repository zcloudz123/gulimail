package com.gulimall.gulimallseckill;

import com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallSeckillApplicationTests {

    @Autowired
    SentinelWebAutoConfiguration sentinelWebAutoConfiguration;

    @Test
    void contextLoads() {
        System.out.println(sentinelWebAutoConfiguration);
    }

}
