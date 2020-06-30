package com.gulimall.gulimallseckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-29-21:41
 */
@Data
public class SeckillSessionWithSkusVo {

    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SeckillSkuVo> skuRelationEntities;
}
