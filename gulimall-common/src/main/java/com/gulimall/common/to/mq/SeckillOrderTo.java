package com.gulimall.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-30-23:25
 */
@Data
public class SeckillOrderTo {

    private String orderSn; //订单号

    private Long promotionSessionId; //活动好

    private Long skuId; //商品号

    private BigDecimal seckillPrice; //秒杀价格

    private Integer num; //数量

    private Long memberId; //会员号

}
