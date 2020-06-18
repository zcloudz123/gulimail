package com.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-16-21:57
 */
@Data
public class SearchParam {

    //全文匹配字符串(skuTitle)
    private String keyword;

    //三级分类Id
    private Long catalog3Id;

    //排序条件(saleCount_asc,hotScore_asc,skuPrice_asc)
    private String sort;

    //过滤条件(0/1),
    private Integer hasStock;

    //价格区间(_500,400_500,500_)
    private String skuPrice;

    //品牌Id
    private List<Long> brandId;

    //属性(2_安卓,3_白色:蓝色)
    private List<String> attrs;

    //分页号
    private Integer pageNum = 1;

    private String _queryString;//原生查询条件
}
