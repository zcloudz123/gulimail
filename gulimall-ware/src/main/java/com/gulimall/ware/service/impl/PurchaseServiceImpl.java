package com.gulimall.ware.service.impl;

import com.gulimall.common.constant.WareConstant;
import com.gulimall.ware.entity.PurchaseDetailEntity;
import com.gulimall.ware.service.PurchaseDetailService;
import com.gulimall.ware.service.WareSkuService;
import com.gulimall.ware.vo.MergeVo;
import com.gulimall.ware.vo.PurchaseDoneItemVo;
import com.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.ware.dao.PurchaseDao;
import com.gulimall.ware.entity.PurchaseEntity;
import com.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().in("status",0,1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream()
                .map(purchaseDetailService::getById)
                .filter(purchaseDetailEntity ->
                        purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATE.getCode()
                                || purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode())
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(purchaseDetailEntities)){
            return;
        }
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null){
            //如果没传采购单id，新建一个
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATE.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> list = purchaseDetailEntities.stream()
                .map(purchaseDetailEntity -> {
            PurchaseDetailEntity purchaseDetailEntity1 = new PurchaseDetailEntity();
            purchaseDetailEntity1.setId(purchaseDetailEntity.getId());
            purchaseDetailEntity1.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity1.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity1;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(list);
        //更新采购单时间
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Transactional
    @Override
    public void received(List<Long> purchaseIds) {
        purchaseIds.stream()
                .map(this::getById)
                //确认当前采购单时新建或者已分配状态
                .filter(purchaseEntity-> purchaseEntity.getStatus()== WareConstant.PurchaseStatusEnum.CREATE.getCode()
                || purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .forEach(purchaseEntity->{
                    //改变采购单的状态
                    purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    purchaseEntity.setUpdateTime(new Date());
                    this.updateById(purchaseEntity);
                    //改变采购详情的状态
                    purchaseDetailService.receivedByPurchaseId(purchaseEntity.getId());
                });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        //根据采购详情区分成功和失败详情
        List<PurchaseDoneItemVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> successDetailEntities = new ArrayList<>();
        List<PurchaseDetailEntity> failureDetailEntities = new ArrayList<>();
        for (PurchaseDoneItemVo item :
                items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item.getItemId());
            purchaseDetailEntity.setStatus(item.getStatus());
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.FINISH.getCode()){
                successDetailEntities.add(purchaseDetailEntity);
            }else{
                failureDetailEntities.add(purchaseDetailEntity);
            }
        }

        //改变采购单状态
        Long id = purchaseDoneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        if(!CollectionUtils.isEmpty(failureDetailEntities)){
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        }else{
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.FINISH.getCode());
        }
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

        //改变采购详情状态
        purchaseDetailService.updateBatchById(successDetailEntities);
        purchaseDetailService.updateBatchById(failureDetailEntities);

        //将采购成功的入库
        successDetailEntities.forEach(purchaseDetailEntity -> {
            PurchaseDetailEntity detail = purchaseDetailService.getById(purchaseDetailEntity.getId());
            wareSkuService.addToStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum());
        });
    }

}