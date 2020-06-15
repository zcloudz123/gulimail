package com.gulimall.search.service;

import com.gulimall.common.to.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-14-21:26
 */
public interface ProductService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;

}
