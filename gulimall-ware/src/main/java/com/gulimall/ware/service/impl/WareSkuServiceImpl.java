package com.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.exception.NoStockException;
import com.gulimall.common.to.SkuHasStockVo;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;
import com.gulimall.common.utils.R;
import com.gulimall.ware.dao.WareSkuDao;
import com.gulimall.ware.entity.WareSkuEntity;
import com.gulimall.ware.feign.ProductFeignService;
import com.gulimall.ware.service.WareSkuService;
import com.gulimall.ware.vo.OrderItemVo;
import com.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId) && !"0".equalsIgnoreCase(skuId)){
            wrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId) && !"0".equalsIgnoreCase(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addToStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> list = this.list(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId)
                .eq("ware_id", wareId));
        if(CollectionUtils.isEmpty(list)){
            //如果没有原库存，创建新库存
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            R r = productFeignService.getNameById(skuId);
            //TODO 异常处理不会回滚的其他方式（不try catch）
            try {
                if(r.getCode() == 0){
                    wareSkuEntity.setSkuName((String) r.get("skuName"));
                    log.warn("远程获取的skuName为"+ r.get("skuName"));
                }else{
                    log.error("远程获取skuName失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.save(wareSkuEntity);
        }else{
            //有库存则在原库存基础上增加
            WareSkuEntity wareSkuEntity = list.get(0);
            wareSkuEntity.setStock(wareSkuEntity.getStock() + skuNum);
            this.updateById(wareSkuEntity);
        }
    }

    @Override
    public List<WareSkuEntity> getWareSkuBySkuId(Long skuId) {
        return this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId));
    }

    @Override
    public List<SkuHasStockVo> skuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId->{
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(skuId);
            Long count = baseMapper.getSkuAvaliableStock(skuId);
            skuHasStockVo.setHasStock(count != null && (count > 0));
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    //默认运行时异常均回滚
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {
        //按照下单的收货地址，找到一个就近的仓库，锁定库存

        //找到每个商品在哪些仓库有库存
        List<OrderItemVo> lockItems = wareSkuLockVo.getLockItems();
        List<SkuWareStockInfo> collect = lockItems.stream().map(orderItemVo -> {
            SkuWareStockInfo skuWareStockInfo = new SkuWareStockInfo();
            Long skuId = orderItemVo.getSkuId();
            List<Long> wareIds = this.listWareIdHasSkuStock(skuId);
            skuWareStockInfo.setSkuId(skuId);
            skuWareStockInfo.setWareIds(wareIds);
            skuWareStockInfo.setLockNum(orderItemVo.getCount());
            return skuWareStockInfo;
        }).collect(Collectors.toList());

        for (SkuWareStockInfo skuWareStockInfo:
             collect) {
            Boolean skuStocked = false;
            Long skuId = skuWareStockInfo.getSkuId();
            Integer lockNum = skuWareStockInfo.getLockNum();
            List<Long> wareIds = skuWareStockInfo.getWareIds();
            if(CollectionUtils.isEmpty(wareIds)){
                //当前商品没有库存直接报异常
                throw new NoStockException(skuId);
            }else{
                for(Long wareId:wareIds){
                    Long count = this.skuWareLockStock(skuId,wareId,lockNum);
                    if(count == 1){
                        skuStocked = true;
                        break;
                    }
                }
            }
            if(!skuStocked){
                //当前商品未成功锁定库存
                throw new NoStockException(skuId);
            }
        }

        return true;
    }

    //查询sku在哪些仓库有库存
    @Override
    public List<Long> listWareIdHasSkuStock(Long skuId) {
        return baseMapper.listWareIdHasSkuStock(skuId);
    }

    @Override
    public Long skuWareLockStock(Long skuId, Long wareId, Integer lockNum) {
        return baseMapper.skuWareLockStock(skuId,wareId,lockNum);
    }

    @Data
    class SkuWareStockInfo{
        private Long skuId;
        private Integer lockNum;
        private List<Long> wareIds;
    }

}