package com.gulimall.gulimalltestssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-20-22:41
 */
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @RequestMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String redirectUrl, Model model, @CookieValue(value = "sso_token",required = false) String token){
        if(token != null){
            return "redirect:" + redirectUrl + "?token=" + token;
        }
        model.addAttribute("url",redirectUrl);
        return "login";
    }

    @RequestMapping("/doLogin")
    public String doLogin(String username, String password, String url, HttpServletResponse response){

        if(!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)){

            String uuid = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(uuid,username);

            //保存登录状态到Cookie中，表示已登录，方便下次检查
            Cookie cookie = new Cookie("sso_token",uuid);
            response.addCookie(cookie);

            //返回给重定向过来的客户端，通知其已登录
            return "redirect:" + url + "?token=" + uuid;
        }
        return "login";

    }
}
