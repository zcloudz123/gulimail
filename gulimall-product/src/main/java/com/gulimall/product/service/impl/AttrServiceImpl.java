package com.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gulimall.common.constant.ProductConstant;
import com.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.gulimall.product.entity.AttrGroupEntity;
import com.gulimall.product.entity.CategoryEntity;
import com.gulimall.product.service.AttrAttrgroupRelationService;
import com.gulimall.product.service.AttrGroupService;
import com.gulimall.product.service.CategoryService;
import com.gulimall.product.vo.AttrRespVo;
import com.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.product.dao.AttrDao;
import com.gulimall.product.entity.AttrEntity;
import com.gulimall.product.service.AttrService;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttrVo(AttrVo attrVo) {
        //保存基本数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);
        this.save(attrEntity);
        //保存关联关系
        if(attrVo.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getMsg().equalsIgnoreCase(attrType) ?
                ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if(catelogId != 0){
            wrapper.eq("catelog_id",catelogId);
        }
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj) ->{
               obj.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> attrRespVos = records.stream().map((attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //设置分组的名字
            if(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getMsg().equalsIgnoreCase(attrType)){
                AttrAttrgroupRelationEntity attrAttrGroupRE = attrAttrgroupRelationService.getOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", attrEntity.getAttrId()));
                if (attrAttrGroupRE != null) {
                    attrRespVo.setGroupName(attrGroupService.getById(attrAttrGroupRE.getAttrGroupId()).getAttrGroupName());
                }
            }

            //设置级联分类的名字
            StringBuilder tempSB = new StringBuilder();
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            while (categoryEntity.getParentCid() != 0) {
                tempSB.append(categoryEntity.getName()).append(",");
                categoryEntity = categoryService.getById(categoryEntity.getParentCid());
            }
            tempSB.append(categoryEntity.getName());

            String[] split = tempSB.toString().split(",");
            StringBuilder categoryName = new StringBuilder();
            for (int i = split.length - 1; i >= 0; i--) {
                categoryName.append(split[i]).append("/");
            }
            categoryName.deleteCharAt(categoryName.length() - 1);
            attrRespVo.setCatelogName(categoryName.toString());
            return attrRespVo;
        })).collect(Collectors.toList());

        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrRespVo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity,attrRespVo);

        //查询得到属性分组的id
        AttrAttrgroupRelationEntity aare = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if(aare != null){
            attrRespVo.setAttrGroupId(aare.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = attrGroupService.getById(aare.getAttrGroupId());
            if(attrEntity != null){
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        //查询得到分类全路径
        attrRespVo.setCatelogPath(categoryService.findCategoryPath(attrEntity.getCatelogId()));
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        if(categoryEntity != null){
            attrRespVo.setCatelogName(categoryEntity.getName());
        }

        return attrRespVo;
    }

    @Override
    public void updateAttrVo(AttrVo attrVo) {
        //修改基本数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);
        this.updateById(attrEntity);
        //修改分组关系
        if(attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            if(attrAttrgroupRelationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrVo.getAttrId())) == 0){
                attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
            }else{
                attrAttrgroupRelationService.update(attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrVo.getAttrId()));
            }
        }
    }

    @Override
    public void removeAttrDetail(Long[] ids) {
        this.removeByIds(Arrays.asList(ids));
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        for (Long id:
             ids) {
            wrapper.or().eq("attr_id",id);
        }
        attrAttrgroupRelationService.remove(wrapper);
    }

}