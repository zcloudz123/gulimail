package com.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-16:23
 */
@ConfigurationProperties("gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;

    private Integer maxSize;

    private Integer keepAliveSeconds;
}
