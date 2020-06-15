package com.gulimall.product.app;

import java.util.List;
import java.util.Map;

import com.gulimall.product.entity.ProductAttrValueEntity;
import com.gulimall.product.service.ProductAttrValueService;
import com.gulimall.product.vo.AttrRespVo;
import com.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gulimall.product.service.AttrService;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.R;



/**
 * 商品属性
 *
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 22:59:08
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baselist(
            @RequestParam Map<String, Object> params,
            @PathVariable("attrType") String attrType,
            @PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrService.queryPage(params);
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,attrType);

        return R.ok().put("page", page);
    }

    @RequestMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(
            @RequestParam Map<String, Object> params,
            @PathVariable("spuId") Long spuId){

        List<ProductAttrValueEntity> list = productAttrValueService.baseAttrListForSpu(spuId);

        return R.ok().put("data", list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
		AttrRespVo attr = attrService.getAttrRespVo(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attrVo){
		attrService.saveAttrVo(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attrVo){
		attrService.updateAttrVo(attrVo);

        return R.ok();
    }

    @PostMapping("/update/{spuId}")
    public R updateSpuAttrs(
            @PathVariable("spuId") Long spuId,
            @RequestBody List<ProductAttrValueEntity> list){
		productAttrValueService.updateSpuAttrs(spuId,list);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeAttrDetail(attrIds);

        return R.ok();
    }

}
