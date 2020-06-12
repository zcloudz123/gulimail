package com.gulimall.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-12-17:08
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
