package com.gulimall.product.service.impl;

import com.gulimall.product.service.AttrService;
import com.gulimall.product.vo.BaseAttrs;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.product.dao.ProductAttrValueDao;
import com.gulimall.product.entity.ProductAttrValueEntity;
import com.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttrValues(Long spuId, List<BaseAttrs> baseAttrs) {
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> list = baseAttrs.stream()
                    .map(baseAttr -> {
                        ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                        productAttrValueEntity.setSpuId(spuId);
                        productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                        productAttrValueEntity.setAttrName(attrService.getById(baseAttr.getAttrId()).getAttrName());
                        productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                        productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                        return productAttrValueEntity;
                    }).collect(Collectors.toList());
            this.saveBatch(list);
        }
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        return this.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));
    }

    @Transactional
    @Override
    public void updateSpuAttrs(Long spuId, List<ProductAttrValueEntity> list) {
        this.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));

        List<ProductAttrValueEntity> collect = list.stream()
                .map(productAttrValueEntity -> {
                    productAttrValueEntity.setSpuId(spuId);
                    return productAttrValueEntity;
                }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}