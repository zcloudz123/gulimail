package com.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.product.dao.SpuImagesDao;
import com.gulimall.product.entity.SpuImagesEntity;
import com.gulimall.product.service.SpuImagesService;
import org.springframework.util.CollectionUtils;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpuImages(Long spuId, List<String> images) {
        if(!CollectionUtils.isEmpty(images)){
            List<SpuImagesEntity> list = images.stream()
                    .map(image -> {
                        SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                        spuImagesEntity.setSpuId(spuId);
                        spuImagesEntity.setImgUrl(image);
                        return spuImagesEntity;
                    }).collect(Collectors.toList());
            this.saveBatch(list);
        }
    }

}