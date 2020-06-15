package com.gulimall.search.controller;

import com.gulimall.common.exception.BizCodeEnum;
import com.gulimall.common.to.SkuEsModel;
import com.gulimall.common.utils.R;
import com.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-14-21:24
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class ElasticSaveController {

    @Autowired
    ProductService productService;

    //上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        try {
            return productService.productStatusUp(skuEsModels)?
                    R.ok():
                    R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        } catch (IOException e) {
            log.error("商品上架失败，原因：" + e.getMessage());
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }

}
