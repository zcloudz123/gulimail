package com.gulimall.gulimallseckill.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.fastjson.JSON;
import com.gulimall.common.exception.BizCodeEnum;
import com.gulimall.common.utils.R;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @decription:
 * @author: zyy
 * @date 2020-07-01-14:48
 */
@Configuration
public class SeckillSentinelConfig {

    @Bean
    public BlockExceptionHandler MyblockExceptionHandler(){
        return (request, response, e) -> {
            R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(JSON.toJSONString(error));
        };
    }


}
