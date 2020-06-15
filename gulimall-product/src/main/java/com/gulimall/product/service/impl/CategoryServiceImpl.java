package com.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;
import com.gulimall.product.dao.CategoryDao;
import com.gulimall.product.entity.CategoryEntity;
import com.gulimall.product.service.CategoryBrandRelationService;
import com.gulimall.product.service.CategoryService;
import com.gulimall.product.vo.Catalog2Vo;
import com.gulimall.product.vo.Catalog3Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        List<CategoryEntity> entities = baseMapper.selectList(null);

        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
        return level1Menus;

    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        //迭代收集父节点id
        CategoryEntity categoryEntity = this.getById(catelogId);
        while (categoryEntity.getParentCid() != 0) {
            paths.add(categoryEntity.getCatId());
            categoryEntity = this.getById(categoryEntity.getParentCid());
        }
        paths.add(categoryEntity.getCatId());

        Collections.reverse(paths);

        return paths.toArray(new Long[]{});
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategoryName(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Category() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        List<CategoryEntity> categoryLevel1 = this.getLevel1Category();
        return categoryLevel1.stream().collect(Collectors.toMap(
                cat -> cat.getCatId().toString(),
                cat -> {
                    //遍历每个一级分类，查找所有2级分类，封装到VO
                    List<CategoryEntity> catalog2Entites = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", cat.getCatId()));
                    return catalog2Entites.stream().map(entity -> {
                        Catalog2Vo catalog2Vo = new Catalog2Vo();
                        catalog2Vo.setCatalog1Id(entity.getParentCid());
                        catalog2Vo.setId(entity.getCatId());
                        catalog2Vo.setName(entity.getName());
                        //遍历每一个2级分类，查询封装3级分类
                        List<Catalog3Vo> catalog3Vos = this.list(
                                new QueryWrapper<CategoryEntity>()
                                        .eq("parent_cid", entity.getCatId())).stream().map(
                                entity1 -> {
                                    Catalog3Vo catalog3Vo = new Catalog3Vo();
                                    catalog3Vo.setId(entity1.getCatId());
                                    catalog3Vo.setCatalog2Id(entity1.getParentCid());
                                    catalog3Vo.setName(entity1.getName());
                                    return catalog3Vo;
                                }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                }));
    }

    private List<CategoryEntity> getChildrens(CategoryEntity entity, List<CategoryEntity> entities) {
        return entities.stream()
                .filter(entity1 -> entity1.getParentCid().equals(entity.getCatId()))
                .map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
    }

}