package com.gulimall.gulimallauthserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gulimall.common.utils.HttpUtils;
import com.gulimall.common.utils.R;
import com.gulimall.gulimallauthserver.feign.MemberFeignService;
import com.gulimall.common.vo.MemberRespVo;
import com.gulimall.gulimallauthserver.vo.WeiBoSocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @decription:
 * response返回的JSON：
 *      "access_token": "2.00UohyZF6xuLJB01ff8640a7I5rT6D",
 *     "remind_in": "157679999",失效时间
 *     "expires_in": 157679999,
 *     "uid": "5112304022", 微博用户Id
 *     "isRealName": "true"
 * @author: zyy
 * @date 2020-06-20-14:49
 */
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @RequestMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        //换取accessToken
        Map<String, String> querys = new HashMap<>();
        querys.put("client_id","1051960411");
        querys.put("client_secret","e5bb75e77982b1ba4fed8afce6f867db");
        querys.put("grant_type","authorization_code");
        querys.put("redirect_uri","http://auth.gulimall.com/oauth2.0/weibo/success");
        querys.put("code",code);

        HttpResponse response = HttpUtils.doPost("https://api.weibo.com",
                "/oauth2/access_token",
                "POST",
                new HashMap<>(),
                querys,
                "");

        //解析响应
        if(response.getStatusLine().getStatusCode() == 200){
            String responseJSON = EntityUtils.toString(response.getEntity());
            WeiBoSocialUser weiBoSocialUser = JSON.parseObject(responseJSON, WeiBoSocialUser.class);

            //得到社交用户信息
            //对第一次进站的用户，自动注册进会员系统，否则查出老用户信息
            R r = memberFeignService.oauthlogin(weiBoSocialUser);
            if(r.getCode() == 0){
                MemberRespVo memberRespVo = r.getData(new TypeReference<MemberRespVo>() {
                });
//                System.out.println("登录成功!" + memberRespVo);
                //TODO 默认是子域存储session，需要修改其作用域
                //TODO 修改session默认的序列化机制，使用JSON存储
                session.setAttribute("loginUser",memberRespVo);
            }else{
                //获取失败重定向到登录页
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else{
            //获取失败重定向到登录页
            return "redirect:http://auth.gulimall.com/login.html";
        }
        return "redirect:http://gulimall.com";
    }
}
