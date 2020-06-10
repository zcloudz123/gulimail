package com.gulimall.product.dao;

import com.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 22:59:08
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
