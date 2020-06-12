package com.gulimall.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-12-16:58
 */
@Data
public class SpuBoundsTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
