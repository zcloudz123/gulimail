package com.gulimall.product.feign;

import com.gulimall.common.utils.R;
import com.gulimall.product.fallback.SeckillFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-30-11:38
 */
@FeignClient(value = "gulimall-seckill",fallback = SeckillFeignServiceFallback.class)
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkukillInfo(@PathVariable("skuId") Long skuId);
}
