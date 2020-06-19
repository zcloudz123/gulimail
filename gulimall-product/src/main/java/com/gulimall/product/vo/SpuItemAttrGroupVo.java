package com.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-11:36
 */
@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
