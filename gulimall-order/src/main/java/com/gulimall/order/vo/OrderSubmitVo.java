package com.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-24-11:26
 */
@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    //无需提交需要购买的商品，从购物车查询
    //优惠、发票
    //防重令牌
    private String orderToken;

    //预计总价，可用于验价
    private BigDecimal payPrice;

    //备注信息
    private String note;

    //用户相关信息，直接去session取出信息

}
