package com.gulimall.gulimallseckill.controller;

import com.gulimall.common.utils.R;
import com.gulimall.gulimallseckill.service.SeckillService;
import com.gulimall.gulimallseckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-30-10:17
 */
@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    //当前时间可以参与的sku信息
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> skuRedisTos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(skuRedisTos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    public R getSkukillInfo(@PathVariable("skuId") Long skuId){

        SeckillSkuRedisTo skuRedisTo = seckillService.getSkukillInfo(skuId);

        return R.ok().setData(skuRedisTo);
    }
}
