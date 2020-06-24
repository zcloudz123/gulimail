package com.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-23-17:35
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String img;
    private List<String> skuAttr;
    private BigDecimal price = new BigDecimal(0);
    private Integer count;
    private BigDecimal totalPrice = new BigDecimal(0);
    private BigDecimal weight;
}
