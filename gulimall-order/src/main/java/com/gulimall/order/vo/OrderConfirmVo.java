package com.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-23-17:30
 */
public class OrderConfirmVo {
    //用户地址
    @Getter @Setter
    private List<MemberAddressVo> addresses;

    //购物项
    @Getter @Setter
    private List<OrderItemVo> items;
    //发票

    //优惠劵
    @Getter @Setter
    private Integer integration;

    //TODO 防重令牌
    @Getter @Setter
    private String orderToken;

    @Getter @Setter
    private Map<Long,Boolean> hasStockMap = new HashMap<>();

    private Integer count;

    public Integer getCount() {
        int count = 0;
        if(!CollectionUtils.isEmpty(items)){
            for (OrderItemVo orderItemVo:
                    items) {
                count += orderItemVo.getCount();
            }
        }
        return count;
    }
    //    private BigDecimal total = new BigDecimal(0);

//    private BigDecimal payPrice = new BigDecimal(0);

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(items)){
            for (OrderItemVo orderItemVo:
                 items) {
                sum = sum.add(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())));
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(items)){
            for (OrderItemVo orderItemVo:
                 items) {
                sum = sum.add(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())));
            }
        }
        return sum;
    }
}
