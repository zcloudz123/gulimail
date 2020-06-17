package com.gulimall.search.vo;

import com.gulimall.common.to.SkuEsModel;
import lombok.Data;

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
