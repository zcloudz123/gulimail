package com.gulimall.product.service.impl;

import com.gulimall.product.vo.ItemSaleAttrsVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.product.dao.SkuSaleAttrValueDao;
import com.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ItemSaleAttrsVo> getSaleAttrsBySpuId(Long spuId) {

        return this.baseMapper.getSaleAttrsBySpuId(spuId);

    }

    @Override
    public List<String> getSkuSaleAttrValuesAskStringList(Long skuId) {

        return this.baseMapper.getSkuSaleAttrValuesAskStringList(skuId);

    }

}