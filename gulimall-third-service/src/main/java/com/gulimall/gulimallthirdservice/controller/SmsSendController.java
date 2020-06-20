package com.gulimall.gulimallthirdservice.controller;

import com.gulimall.common.utils.R;
import com.gulimall.gulimallthirdservice.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-18:26
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/sendCode")
    public R sendCode(
            @RequestParam("phone") String phone,
            @RequestParam("code") String code){
        smsComponent.sendSmsCode(phone,code);
        return R.ok();
    }
}
