package com.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.constant.ProductConstant;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;
import com.gulimall.product.dao.AttrGroupDao;
import com.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.gulimall.product.entity.AttrEntity;
import com.gulimall.product.entity.AttrGroupEntity;
import com.gulimall.product.service.AttrAttrgroupRelationService;
import com.gulimall.product.service.AttrGroupService;
import com.gulimall.product.service.AttrService;
import com.gulimall.product.vo.AttrGroupRelationVo;
import com.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.gulimall.product.vo.SkuItemVo;
import com.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Integer categoryId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0) {
            wrapper.eq("catelog_id", categoryId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(obj -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> getRelationAttrs(Long attrGroupId) {
        List<Long> attrIds = attrAttrgroupRelationService.list(
                new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_group_id", attrGroupId))
                .stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());
        List<AttrEntity> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(attrIds)) {
            list.addAll(attrService.list(
                    new QueryWrapper<AttrEntity>()
                            .in("attr_id", attrIds)
                            .eq("attr_type", ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getCode())));
        }
        return list;
    }

    @Override
    public void removeRelation(AttrGroupRelationVo[] attrGroupRelationVos) {
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        for (AttrGroupRelationVo attrGroupRelationVo :
                attrGroupRelationVos) {
            wrapper.or((obj) -> {
                obj.eq("attr_id", attrGroupRelationVo.getAttrId())
                        .eq("attr_group_id", attrGroupRelationVo.getAttrGroupId());
            });
        }
        attrAttrgroupRelationService.remove(wrapper);
    }

    @Override
    public PageUtils getNoRelationAttrs(Map<String, Object> params, Long attrGroupId) {
        //当前分组只能关联自己所属分类里面的属性
        AttrGroupEntity attrGroupEntity = this.getById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //当前分组只能关联别的分组没有引用的属性

        //查到同一分类下的所有分组id
        List<Long> allAttrGroupIds = this.list(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId))
                .stream()
                .map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList());

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getCode());

        if (!CollectionUtils.isEmpty(allAttrGroupIds)) {
            //同一分类下的所有属性id
            List<Long> allAttrIds = attrAttrgroupRelationService.list(
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .in("attr_group_id", allAttrGroupIds))
                    .stream()
                    .map(AttrAttrgroupRelationEntity::getAttrId)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(allAttrIds)) {
                wrapper.notIn("attr_id", allAttrIds);
            }
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> w.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void saveRelation(AttrGroupRelationVo[] attrGroupRelationVos) {
        List<AttrAttrgroupRelationEntity> entities = new ArrayList<>();
        for (AttrGroupRelationVo attrGroupRelationVo :
                attrGroupRelationVos) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attrGroupRelationVo, attrAttrgroupRelationEntity);
            entities.add(attrAttrgroupRelationEntity);

        }
        attrAttrgroupRelationService.saveBatch(entities);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatalogId(Integer catalogId) {
        //查出当前分类下的所有分组
        List<AttrGroupEntity> attrGroups = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catalogId));

        //查出各个分组下当前分类的所有属性，并组合
        return attrGroups.stream().map(attrGroupEntity ->{
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroupEntity,attrGroupWithAttrsVo);
            List<AttrEntity> attrs = attrAttrgroupRelationService.list(
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_group_id", attrGroupEntity.getAttrGroupId()))
                    .stream()
                    .map(attrAttrgroupRelationEntity ->
                            attrService.getById(attrAttrgroupRelationEntity.getAttrId())
                    ).collect(Collectors.toList());
            attrGroupWithAttrsVo.setAttrs(attrs);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsByspuId(Long spuId, Long catalogId) {
        //查出当前spu对应的所有属性的分组信息及各分组下的属性信息
        return this.baseMapper.getAttrGroupWithAttrsByspuId(spuId,catalogId);

    }

}