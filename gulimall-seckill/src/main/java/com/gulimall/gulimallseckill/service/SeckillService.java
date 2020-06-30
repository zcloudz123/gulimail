package com.gulimall.gulimallseckill.service;

import com.gulimall.gulimallseckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-29-21:09
 */
public interface SeckillService {

    void uploadSeckillLastest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkukillInfo(Long skuId);

}
