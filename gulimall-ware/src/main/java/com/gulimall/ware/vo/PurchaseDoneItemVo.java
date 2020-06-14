package com.gulimall.ware.vo;

import lombok.Data;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-13-9:58
 */
@Data
public class PurchaseDoneItemVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
