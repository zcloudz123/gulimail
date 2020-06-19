package com.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-13:02
 */
@Data
public class ItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdsVo> attrValues;
}
