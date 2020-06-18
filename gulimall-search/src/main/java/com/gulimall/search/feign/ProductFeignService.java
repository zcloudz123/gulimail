package com.gulimall.search.feign;

import com.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-17-19:09
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);

    @RequestMapping("/product/brand/infos")
    R brandsInfo(@RequestParam("brandIds") List<Long> brandIds);
}
