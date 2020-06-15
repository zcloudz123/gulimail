package com.gulimall.product.feign;

import com.gulimall.common.to.SkuEsModel;
import com.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-14-21:46
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
