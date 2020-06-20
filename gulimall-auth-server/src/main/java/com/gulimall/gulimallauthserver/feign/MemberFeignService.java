package com.gulimall.gulimallauthserver.feign;

import com.gulimall.common.utils.R;
import com.gulimall.gulimallauthserver.vo.UserLoginVo;
import com.gulimall.gulimallauthserver.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-20-9:49
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo userRegistVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);
}
