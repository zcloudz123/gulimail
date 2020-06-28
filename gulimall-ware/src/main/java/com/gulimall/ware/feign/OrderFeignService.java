package com.gulimall.ware.feign;

import com.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-28-10:29
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @ResponseBody
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
