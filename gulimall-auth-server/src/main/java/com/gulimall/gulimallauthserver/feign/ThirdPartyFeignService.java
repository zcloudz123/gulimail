package com.gulimall.gulimallauthserver.feign;

import com.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-18:31
 */
@FeignClient("gulimall-third-service")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone,
            @RequestParam("code") String code);
}
