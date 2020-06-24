package com.gulimall.ware.vo;

import lombok.Data;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-24-16:14
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
