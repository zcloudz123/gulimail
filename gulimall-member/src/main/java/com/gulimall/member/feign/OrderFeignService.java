package com.gulimall.member.feign;

import com.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-28-21:18
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @RequestMapping("/order/order/listWithItems")
    R listWithItems(@RequestBody Map<String, Object> params);
}
