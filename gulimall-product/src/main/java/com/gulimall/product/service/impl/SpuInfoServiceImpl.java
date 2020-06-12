package com.gulimall.product.service.impl;

import com.gulimall.common.to.SkuReductionTo;
import com.gulimall.common.to.SpuBoundsTo;
import com.gulimall.common.utils.R;
import com.gulimall.product.entity.*;
import com.gulimall.product.feign.CouponFeignService;
import com.gulimall.product.service.*;
import com.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * TODO 远程方法调用失败后事务回滚需处理
     * @param spuSaveVo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //保存spu基本信息spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        //保存spu描述图片spu_des
        List<String> decripts = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",",decripts));
        spuInfoDescService.save(spuInfoDescEntity);

        //保存spu图片集spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveSpuImages(spuId,images);

        //保存spu的属性spu_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        productAttrValueService.saveAttrValues(spuId,baseAttrs);

        //保存spu的积分信息sms:spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds,spuBoundsTo);
        spuBoundsTo.setSpuId(spuId);
        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if(r.getCode() != 0){
            log.error("spu积分信息保存失败");
        }

        //保存spu的对应的所有sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if(!CollectionUtils.isEmpty(skus)){
            skus.forEach(sku->{

                //sku基本信息sku_info
                String defaultImage = "";
                for (Images image :
                        sku.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImage = image.getImgUrl();
                    }
                }
                //spuId,catalogId,brandId,defaultImage,
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku,skuInfoEntity);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //sku图片信息sku_images
                List<SkuImagesEntity> skuImages = sku.getImages().stream()
                        //过滤掉不使用的图片
                        .filter(image -> !StringUtils.isEmpty(image.getImgUrl()))
                        .map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(image.getImgUrl());
                            skuImagesEntity.setDefaultImg(image.getDefaultImg());
                            return skuImagesEntity;
                        })
                        .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(skuImages)){
                    skuImagesService.saveBatch(skuImages);
                }

                //sku销售属性信息sku_sale_attr_value
                List<SkuSaleAttrValueEntity> skuSaleAttrValues = sku.getAttr().stream()
                        .map(attr -> {
                            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                            BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                            skuSaleAttrValueEntity.setSkuId(skuId);
                            return skuSaleAttrValueEntity;
                        }).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(skuSaleAttrValues)){
                    skuSaleAttrValueService.saveBatch(skuSaleAttrValues);
                }
                //sku的优惠、满减信息sms:sku_ladder/sku_full_reduction调用远程接口，只要有优惠就发送
                long count = sku.getMemberPrice().stream()
                        .filter(memberPrice -> memberPrice.getPrice().compareTo(new BigDecimal(0)) > 0)
                        .count();
                if(sku.getFullPrice().compareTo(new BigDecimal(0)) > 0 || sku.getFullCount() > 0 || count > 0){
                    SkuReductionTo skuReductionTo = new SkuReductionTo();
                    BeanUtils.copyProperties(sku,skuReductionTo);
                    skuReductionTo.setSkuId(skuId);
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("sku优惠信息保存失败");
                    }
                }
            });
        }

    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}