package com.gulimall.ware.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.tm.api.GlobalTransactionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //给新请求同步老请求的cookie
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes != null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    if(request != null){
                        requestTemplate.header("Cookie",request.getHeader("Cookie"));
                    }
                    String xid = GlobalTransactionContext.getCurrentOrCreate().getXid();
                    if(xid != null){
                        requestTemplate.header("TX_XID",xid);
                    }
                }
            }
        };
    }

}