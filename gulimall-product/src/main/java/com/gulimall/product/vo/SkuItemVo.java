package com.gulimall.product.vo;

import com.gulimall.product.entity.SkuImagesEntity;
import com.gulimall.product.entity.SkuInfoEntity;
import com.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-9:54
 */
@ToString
@Data
public class SkuItemVo {
    SkuInfoEntity info;

    boolean hasStock = true;

    List<SkuImagesEntity> images;

    List<ItemSaleAttrsVo> saleAttrs;

    SpuInfoDescEntity desp;

    List<SpuItemAttrGroupVo> attrGroups;

}
