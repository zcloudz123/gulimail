package com.gulimall.gulimalltestssoclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-20-22:29
 */
@Controller
public class HelloController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Value("${sso.server.url}")
    String ssoServerUrl;

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    //需感知ssoserver重定向回来
    @GetMapping("/boss")
    public String employees(Model model, HttpSession session,@RequestParam(value = "token",required = false) String token){

        Object loginUser = session.getAttribute("loginUser");
        if(loginUser == null){
            if(StringUtils.isEmpty(token)){
                return "redirect:" + ssoServerUrl + "?redirect_url=" + "http://client2.com:8082/boss";
            }else{
                redisTemplate.opsForValue().get(token);
                session.setAttribute("loginUser","zhangsan");
            }
        }

        List<String> emps = new ArrayList<>();
        emps.add("张三");
        emps.add("李四");
        model.addAttribute("emps",emps);
        return "list";
    }

}
