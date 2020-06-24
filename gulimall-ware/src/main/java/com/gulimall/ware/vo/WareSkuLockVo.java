package com.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-24-16:11
 */
@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> lockItems;
}
