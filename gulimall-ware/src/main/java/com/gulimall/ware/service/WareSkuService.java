package com.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gulimall.common.to.mq.OrderTo;
import com.gulimall.common.to.mq.StockLockedTo;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.ware.entity.WareSkuEntity;
import com.gulimall.common.to.SkuHasStockVo;
import com.gulimall.ware.vo.LockStockResult;
import com.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 23:20:19
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addToStock(Long skuId, Long wareId, Integer skuNum);

    List<WareSkuEntity> getWareSkuBySkuId(Long skuId);

    List<SkuHasStockVo> skuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo wareSkuLockVo);

    List<Long> listWareIdHasSkuStock(Long skuId);

    Long skuWareLockStock(Long skuId, Long wareId, Integer lockNum);

    void unlockStock(StockLockedTo stockLockedTo);

    void unlockStock(OrderTo orderTo);
}

