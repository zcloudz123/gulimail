package com.gulimall.gulimallseckill.scheduled;

import com.gulimall.gulimallseckill.feign.CouponFeignService;
import com.gulimall.gulimallseckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @decription:
 * 每晚3点上架最近三天需要上架的商品
 * @author: zyy
 * @date 2020-06-29-21:04
 */
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillLastest3Days(){
        //重复上架无需处理 TODO 保证接口幂等
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillLastest3Days();
        }finally {
            lock.unlock();
        }
    }
}
