package com.gulimall.order.feign;

import com.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-23-18:07
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
