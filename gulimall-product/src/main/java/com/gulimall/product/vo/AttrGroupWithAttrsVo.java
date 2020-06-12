package com.gulimall.product.vo;

import com.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-12-11:49
 */
@Data
public class AttrGroupWithAttrsVo {
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
