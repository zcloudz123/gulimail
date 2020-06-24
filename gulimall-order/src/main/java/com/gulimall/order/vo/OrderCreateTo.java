package com.gulimall.order.vo;

import com.gulimall.order.entity.OrderEntity;
import com.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-24-14:50
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    //订单支付
    private BigDecimal payPrice;

    //运费
    private BigDecimal fare;
}
