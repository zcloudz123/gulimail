package com.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-24-10:22
 */
@Data
public class FareVo {
    private MemberAddressVo memberAddressVo;
    private BigDecimal fare;
}
