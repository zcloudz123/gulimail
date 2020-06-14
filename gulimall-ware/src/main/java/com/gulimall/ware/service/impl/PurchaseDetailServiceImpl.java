package com.gulimall.ware.service.impl;

import com.gulimall.common.constant.WareConstant;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.ware.dao.PurchaseDetailDao;
import com.gulimall.ware.entity.PurchaseDetailEntity;
import com.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("purchase_id",key).or().eq("sku_id",key);
            });
        }
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status) && !"0".equalsIgnoreCase(status)){
            wrapper.eq("status",status);
        }
        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId) && !"0".equalsIgnoreCase(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void receivedByPurchaseId(Long id) {
        List<PurchaseDetailEntity> purchaseDetailEntities = this.list(
                new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        List<PurchaseDetailEntity> collect = purchaseDetailEntities.stream()
                .map(purchaseDetailEntity -> {
                    PurchaseDetailEntity purchaseDetailEntity1 = new PurchaseDetailEntity();
                    purchaseDetailEntity1.setId(purchaseDetailEntity.getId());
                    purchaseDetailEntity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    return purchaseDetailEntity1;
                }).collect(Collectors.toList());
        this.updateBatchById(collect);
    }

}