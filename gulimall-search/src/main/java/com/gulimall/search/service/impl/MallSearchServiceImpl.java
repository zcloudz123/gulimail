package com.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gulimall.common.to.SkuEsModel;
import com.gulimall.common.utils.R;
import com.gulimall.search.config.ElasticSearchConfig;
import com.gulimall.search.constant.EsConstant;
import com.gulimall.search.feign.ProductFeignService;
import com.gulimall.search.service.MallSearchService;
import com.gulimall.search.vo.AttrResponseVo;
import com.gulimall.search.vo.SearchParam;
import com.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-16-21:59
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchRequest searchRequest = buildSearchRequest(param);

        SearchResult searchResult = null;
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            searchResult = buildSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult searchResult = new SearchResult();

        //封装商品搜索模型
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if(hits.getHits() != null && hits.getHits().length > 0){
            for(SearchHit hit : hits.getHits()){
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if(!StringUtils.isEmpty(param.getKeyword()))
                {
                    String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }
                esModels.add(skuEsModel);
            }
        }
        searchResult.setProducts(esModels);

        //封装聚合模型

        //属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        attr_id_agg.getBuckets().forEach(bucket->{
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        });
        searchResult.setAttrs(attrVos);

        //品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        brand_agg.getBuckets().forEach(bucket->{
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long brandId = bucket.getKeyAsNumber().longValue();
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        });
        searchResult.setBrands(brandVos);

        //分类
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        catalog_agg.getBuckets().forEach(bucket->{
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            long catalogId = bucket.getKeyAsNumber().longValue();
            String catalogName = ((ParsedStringTerms) bucket.getAggregations().get("catalog_name_agg")).getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogId(catalogId);
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        });
        searchResult.setCatalogs(catalogVos);

        searchResult.setPageNum(param.getPageNum());

        long total = hits.getTotalHits().value;
        searchResult.setTotal(total);

        int totalPages = (int) ((total + EsConstant.PRODUCT_PAGESIZE - 1) / EsConstant.PRODUCT_PAGESIZE);
        searchResult.setTotalPages(totalPages);

        // 构建遍历需要的页码
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        // 构建面包屑属性导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(item -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = item.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.info(Long.parseLong(s[0]));

                // 封装当前请求参数中已经选择的属性的id - 为了前台显示的时候排除显示这些id
                searchResult.getAttrIds().add(Long.parseLong(s[0]));

                if (r.getCode() == 0) {
                    AttrResponseVo responseVo = r.getData("attr",new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(responseVo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                // 取消面包屑以后，重新设置面包屑需要显示的url地址
                String replace = replaceQueryString(param, item, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());

            searchResult.setNavs(navVos);
        }

        // 构建品牌和分类的面包屑导航
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = searchResult.getNavs();

            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<SearchResult.BrandVo> brands = r.getData("brands",new TypeReference<List<SearchResult.BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = null;
                for (SearchResult.BrandVo brand : brands) {
                    buffer.append(brand.getBrandName()+";");

                    replace = replaceQueryString(param, brand.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());

                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

            }
            navs.add(navVo);

        }

        return searchResult;
    }

    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder source = new SearchSourceBuilder();
        //准备数据

        //查询:模糊匹配、过滤（属性、分类、品牌、价格区间、库存）
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        if (param.getCatalog3Id() != null) {
            boolQuery.filter().add(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter().add(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //属性筛选(2_安卓,3_白色:蓝色)
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            for (String attr :
                    param.getAttrs()) {
                BoolQueryBuilder nestedQueryBool = QueryBuilders.boolQuery();
                String[] split = attr.split("_");
                nestedQueryBool.must().add(QueryBuilders.termQuery("attrs.attrId", split[0]));
                nestedQueryBool.must().add(QueryBuilders.termsQuery("attrs.attrValue", split[1].split(":")));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedQueryBool, ScoreMode.None);
                boolQuery.filter().add(nestedQuery);
            }
        }

        if(param.getHasStock() != null){
            boolQuery.filter().add(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder skuPriceRange = QueryBuilders.rangeQuery("skuPrice");
            String[] split = param.getSkuPrice().split("_");
            String lte = split.length <= 1?null:split[1];
            if (!StringUtils.isEmpty(split[0])) {
                skuPriceRange.gte(split[0]);
            }
            if (!StringUtils.isEmpty(lte)) {
                skuPriceRange.lte(lte);
            }
            boolQuery.filter().add(skuPriceRange);
        }

        source.query(boolQuery);

        //排序、分页、高亮
        if(!StringUtils.isEmpty(param.getSort())){
            String[] split = param.getSort().split("_");
            source.sort(split[0],"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
        }
        source.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        source.size(EsConstant.PRODUCT_PAGESIZE);

        if(!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlighter = new HighlightBuilder();
            highlighter.field("skuTitle");
            highlighter.preTags("<b style='color:red'>");
            highlighter.postTags("</b>");
            source.highlighter(highlighter);
        }

        //聚合分析(品牌，属性，分类)-》为进一步检索提供准备相当于sql中的group by

        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        source.aggregation(brand_agg);

        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(50);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        source.aggregation(catalog_agg);

        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        source.aggregation(attr_agg);

        System.out.println("构建的DSL：" + source);

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, source);

    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        return param.get_queryString().replace("&" + key + "=" + encode, "");
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
