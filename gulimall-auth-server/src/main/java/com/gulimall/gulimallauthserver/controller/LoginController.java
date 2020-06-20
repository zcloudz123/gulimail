package com.gulimall.gulimallauthserver.controller;

import com.gulimall.common.constant.AuthServerConstant;
import com.gulimall.common.exception.BizCodeEnum;
import com.gulimall.common.utils.R;
import com.gulimall.gulimallauthserver.feign.MemberFeignService;
import com.gulimall.gulimallauthserver.feign.ThirdPartyFeignService;
import com.gulimall.gulimallauthserver.vo.UserLoginVo;
import com.gulimall.gulimallauthserver.vo.UserRegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-17:17
 */
@Controller
@Slf4j
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        //TODO 接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_COD_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            String[] split = redisCode.split("_");
            long saveTime = Long.parseLong(split[1]);
            if (System.currentTimeMillis() - saveTime < 60 * 1000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //验证码的再次校验
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        R r = thirdPartyFeignService.sendCode(phone, code);

        if (r.getCode() != 0) {
            log.error("发送短信远程调用失败");
        } else {
            //缓存验证码键为phone，值为code
            redisCode = code + "_" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(AuthServerConstant.SMS_COD_CACHE_PREFIX + phone, redisCode, 15, TimeUnit.MINUTES);
        }
        return R.ok();
    }

    /**
     * //TODO 重定向携带数据，利用session原理，将数据放在session中，一次性读取（分布式session问题）
     * redirectAttributes  重定向的共享数据类
     *
     * @param userRegistVo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes redirectAttributes) {
        //注册成功返回登录页
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

//            model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors", errors);
            //校验出错转发到注册页(注意：不能使用转发，POSt请求转发到只能接收GET请求的Controller会引发方法错误)
            //重定向可解决表单重复提交的问题
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //注册
        //校验验证码
        String code = userRegistVo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_COD_CACHE_PREFIX + userRegistVo.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            if (code.equals(redisCode.split("_")[0])) {
                redisTemplate.delete(AuthServerConstant.SMS_COD_CACHE_PREFIX + userRegistVo.getPhone());
                //验证码正确,调用远程注册
                R r = memberFeignService.regist(userRegistVo);
                if (r.getCode() != 0) {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", (String)r.get("msg"));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }else {
                    //正确注册
                    return "redirect:http://auth.gulimall.com/login.html";
                }

            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", BizCodeEnum.SMS_CODE_INVALID_EXCEPTION.getMsg());
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", BizCodeEnum.SMS_CODE_INVALID_EXCEPTION.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo,RedirectAttributes redirectAttributes){

        //远程登录
        R r = memberFeignService.login(userLoginVo);
        if(r.getCode() == 0){

            //session

            return "redirect:http://gulimall.com";
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String)r.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
