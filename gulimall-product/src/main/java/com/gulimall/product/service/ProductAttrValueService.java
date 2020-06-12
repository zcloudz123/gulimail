package com.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.product.entity.ProductAttrValueEntity;
import com.gulimall.product.vo.BaseAttrs;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 22:59:08
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttrValues(Long spuId, List<BaseAttrs> baseAttrs);

}

