package com.gulimall.product.feign;

import com.gulimall.common.to.SkuReductionTo;
import com.gulimall.common.to.SpuBoundsTo;
import com.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-12-16:55
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/save/reduction")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
