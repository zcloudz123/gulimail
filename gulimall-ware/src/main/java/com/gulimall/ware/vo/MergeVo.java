package com.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-12-22:36
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
