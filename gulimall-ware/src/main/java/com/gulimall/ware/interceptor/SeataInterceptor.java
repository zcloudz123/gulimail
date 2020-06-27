package com.gulimall.ware.interceptor;

import io.seata.core.context.RootContext;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-27-10:32
 */
public class SeataInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String xid = request.getHeader("TX_XID");
        if(!StringUtils.isEmpty(xid)){
            RootContext.bind(xid);
        }
        return true;
    }
}
