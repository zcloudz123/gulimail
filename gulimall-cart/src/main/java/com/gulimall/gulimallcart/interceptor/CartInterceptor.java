package com.gulimall.gulimallcart.interceptor;

import com.gulimall.common.constant.AuthServerConstant;
import com.gulimall.common.constant.CartConstant;
import com.gulimall.common.vo.MemberRespVo;
import com.gulimall.gulimallcart.vo.UserInfoTo;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @decription:在controller执行前使用拦截器检查session状态，获取用户信息
 * @author: zyy
 * @date 2020-06-22-10:01
 */
public class CartInterceptor extends HandlerInterceptorAdapter {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        MemberRespVo memberRespVo = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberRespVo != null) {
            //已登录
            userInfoTo.setUserId(memberRespVo.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie :
                    cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setHasTempUser(true);
                }
            }
        }
        //没有临时用户，创建临时用户
        if(BooleanUtils.isNotTrue(userInfoTo.getHasTempUser())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        threadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if(BooleanUtils.isNotTrue(userInfoTo.getHasTempUser())){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_EXPIRE_SECONDS);
            response.addCookie(cookie);
        }
    }
}
