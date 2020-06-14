package com.gulimall.ware.feign;

import com.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-13-10:33
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @PostMapping("/product/skuinfo/info/name")
    R getNameById(@RequestBody Long skuId);
}
