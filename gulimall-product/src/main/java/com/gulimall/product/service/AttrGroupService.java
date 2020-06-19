package com.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.product.entity.AttrEntity;
import com.gulimall.product.entity.AttrGroupEntity;
import com.gulimall.product.vo.AttrGroupRelationVo;
import com.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.gulimall.product.vo.SkuItemVo;
import com.gulimall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 22:59:08
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Integer categoryId);

    List<AttrEntity> getRelationAttrs(Long attrGroupId);

    void removeRelation(AttrGroupRelationVo[] attrGroupRelationVos);

    PageUtils getNoRelationAttrs(Map<String, Object> params, Long attrGroupId);

    void saveRelation(AttrGroupRelationVo[] attrGroupRelationVos);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatalogId(Integer catalogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsByspuId(Long spuId, Long catalogId);

}

