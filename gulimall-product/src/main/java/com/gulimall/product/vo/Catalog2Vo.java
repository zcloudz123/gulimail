package com.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-15-16:11
 */
@Data
public class Catalog2Vo {
    private Long catalog1Id;
    private List<Catalog3Vo> catalog3List;
    private Long id;
    private String name;

}
