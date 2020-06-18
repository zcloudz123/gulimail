package com.gulimall.search.vo;

import com.gulimall.common.to.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-17-13:37
 */
@Data
public class SearchResult {
    private List<SkuEsModel> products;
    private List<AttrVo> attrs;
    private List<BrandVo> brands;
    private List<CatalogVo> catalogs;
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs; // 用于遍历页码
    private List<NavVo> navs = new ArrayList<>(); //面包屑导航数据
    private List<Long> attrIds = new ArrayList<>();


    @Data
    public static class NavVo{

        private String navName;

        private String navValue;

        private String link;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
