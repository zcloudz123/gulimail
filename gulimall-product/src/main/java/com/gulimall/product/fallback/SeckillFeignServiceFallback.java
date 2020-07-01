package com.gulimall.product.fallback;

import com.gulimall.common.exception.BizCodeEnum;
import com.gulimall.common.utils.R;
import com.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @decription:
 * @author: zyy
 * @date 2020-07-01-22:22
 */
@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {
    @Override
    public R getSkukillInfo(Long skuId) {
        log.info("熔断方法调用");
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(),BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    }
}
