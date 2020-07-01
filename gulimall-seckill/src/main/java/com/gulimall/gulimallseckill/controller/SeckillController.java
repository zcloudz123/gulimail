package com.gulimall.gulimallseckill.controller;

import com.gulimall.common.utils.R;
import com.gulimall.gulimallseckill.service.SeckillService;
import com.gulimall.gulimallseckill.to.SeckillSkuRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-30-10:17
 */
@Slf4j
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/kill")
    public String seckill(
            @RequestParam("killId") String killId,
            @RequestParam("code") String code,
            @RequestParam("num") Integer num,
            Model model){
        String orderSn = seckillService.kill(killId,code,num);

        model.addAttribute("orderSn",orderSn);
        return "success";
    }

    //当前时间可以参与的sku信息
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        log.info("currentSeckillSkus正在执行");

        List<SeckillSkuRedisTo> skuRedisTos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(skuRedisTos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkukillInfo(@PathVariable("skuId") Long skuId){

        SeckillSkuRedisTo skuRedisTo = seckillService.getSkukillInfo(skuId);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return R.ok().setData(skuRedisTo);
    }
}
