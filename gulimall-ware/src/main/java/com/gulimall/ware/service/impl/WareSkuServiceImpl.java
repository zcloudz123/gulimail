package com.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.exception.NoStockException;
import com.gulimall.common.to.SkuHasStockVo;
import com.gulimall.common.to.mq.OrderTo;
import com.gulimall.common.to.mq.StockDetailTo;
import com.gulimall.common.to.mq.StockLockedTo;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;
import com.gulimall.common.utils.R;
import com.gulimall.ware.dao.WareSkuDao;
import com.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.gulimall.ware.entity.WareOrderTaskEntity;
import com.gulimall.ware.entity.WareSkuEntity;
import com.gulimall.ware.feign.OrderFeignService;
import com.gulimall.ware.feign.ProductFeignService;
import com.gulimall.ware.service.WareOrderTaskDetailService;
import com.gulimall.ware.service.WareOrderTaskService;
import com.gulimall.ware.service.WareSkuService;
import com.gulimall.ware.vo.OrderItemVo;
import com.gulimall.ware.vo.OrderVo;
import com.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

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

        //保存库存工作单
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

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
                //如果都锁定成功，MQ会缓存所有锁定的工作单
                //锁定失败，前面的保存的工作单就回滚了，MQ中的工作单会找不到detailId
                for(Long wareId:wareIds){
                    Long count = this.skuWareLockStock(skuId,wareId,lockNum);
                    if(count == 1){
                        skuStocked = true;
                        //告诉MQ库存锁定成功,每个仓库锁定对应sku的详情信息
                        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null, skuId, "", lockNum, wareOrderTaskEntity.getId(), wareId, 1);
                        wareOrderTaskDetailService.save(detailEntity);
                        StockLockedTo stockLockedTo = new StockLockedTo();
                        stockLockedTo.setTaskId(wareOrderTaskEntity.getId());
                        StockDetailTo stockDetailTo = new StockDetailTo();
                        BeanUtils.copyProperties(detailEntity,stockDetailTo);
                        stockLockedTo.setStockDetailTo(stockDetailTo);

                        rabbitTemplate.convertAndSend("stock-event-exchange","stock.create.stock",stockLockedTo);
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

    @Transactional
    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {
        StockDetailTo stockDetailTo = stockLockedTo.getStockDetailTo();
        Long detailToId = stockDetailTo.getId();

        //查询工作单中detail信息
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailToId);
        if(detailEntity == null){
            //说明库存锁定失败，工作单已经回滚,不处理
        }else {
            //说明库存锁定成功，再查询订单状态
            Long taskId = stockLockedTo.getTaskId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if(r.getCode() == 0){
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                });
                //订单不存在，或者订单已取消
                if(orderVo == null || orderVo.getStatus() == 4){
                    //订单不存在或者已取消，仅在已锁定状态 解锁库存
                    if(detailEntity.getLockStatus() == 1){
                        unlockStock(stockDetailTo.getSkuId(),stockDetailTo.getWareId(),stockDetailTo.getSkuNum(),stockDetailTo.getId());
                    }
                }
            }else{
                throw new RuntimeException("远程服务失败");
            }
        }
    }

    //订单的取消可能出现异常情况在解锁库存之后才生效，可以在取消后直接解锁
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderTo.getOrderSn());
        if(wareOrderTaskEntity != null){
            List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.getLockedOrderTaskDetailByTaskId(wareOrderTaskEntity.getId());
            for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
                unlockStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum(),detailEntity.getId());
            }
        }
    }

    private void unlockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        baseMapper.unlockStock(skuId,wareId,num);
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

    @Data
    class SkuWareStockInfo{
        private Long skuId;
        private Integer lockNum;
        private List<Long> wareIds;
    }

}