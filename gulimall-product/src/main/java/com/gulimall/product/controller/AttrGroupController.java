package com.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.gulimall.product.entity.AttrEntity;
import com.gulimall.product.service.CategoryService;
import com.gulimall.product.vo.AttrGroupRelationVo;
import com.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gulimall.product.entity.AttrGroupEntity;
import com.gulimall.product.service.AttrGroupService;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.R;



/**
 * 属性分组
 *
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 22:59:08
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    public R list(
            @RequestParam Map<String, Object> params,
            @PathVariable("categoryId") Integer categoryId){
//        PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params,categoryId);

        return R.ok().put("page", page);
    }

    @GetMapping("/{catalogId}/withattr")
    public R getAttrGroupWithAttrs(
            @PathVariable("catalogId") Integer catalogId){
        List<AttrGroupWithAttrsVo> attrGroups = attrGroupService.getAttrGroupWithAttrsByCatalogId(catalogId);

        return R.ok().put("data", attrGroups);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();

        Long[] categoryPath = categoryService.findCategoryPath(catelogId);

        attrGroup.setCatelogPath(categoryPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    //根据分组查询对应的所有属性
    @GetMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> attrs = attrGroupService.getRelationAttrs(attrGroupId);

        return R.ok().put("data", attrs);
    }

    //根据分组查询对应的所有无关联属性
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R noAttrRelation(
            @RequestParam Map<String, Object> params,
            @PathVariable("attrGroupId") Long attrGroupId){
        PageUtils page = attrGroupService.getNoRelationAttrs(params,attrGroupId);

        return R.ok().put("page", page);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    //保存属性分组关联
    @PostMapping("/attr/relation")
    public R saveAttrRelation(
            @RequestBody AttrGroupRelationVo[] attrGroupRelationVos){
		attrGroupService.saveRelation(attrGroupRelationVos);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    //attrId, attrGroupId
    @PostMapping("/attr/relation/delete")
    public R attrRelationDel(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos){
		attrGroupService.removeRelation(attrGroupRelationVos);

        return R.ok();
    }

}
