package com.gulimall.product.vo;

import lombok.Data;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-11-17:53
 */
@Data
public class AttrRespVo extends AttrVo{
    //分类级联名
    private String catelogName;

    //属性分组名
    private String groupName;

    //分类路径
    private Long[] catelogPath;
}
