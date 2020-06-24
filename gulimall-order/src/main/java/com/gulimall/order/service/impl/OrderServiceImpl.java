package com.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.gulimall.common.to.SkuHasStockVo;
import com.gulimall.common.utils.R;
import com.gulimall.common.vo.MemberRespVo;
import com.gulimall.order.feign.CartFeignService;
import com.gulimall.order.feign.MemberFeignService;
import com.gulimall.order.feign.WareFeignService;
import com.gulimall.order.interceptor.LoginUserInterceptor;
import com.gulimall.order.vo.MemberAddressVo;
import com.gulimall.order.vo.OrderConfirmVo;
import com.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.order.dao.OrderDao;
import com.gulimall.order.entity.OrderEntity;
import com.gulimall.order.service.OrderService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeignService wareFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询用户地址
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddresses(address);
        }, executor);

        CompletableFuture<Void> cartItemsTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(()->{
            List<Long> skuIds = confirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.hasStock(skuIds);
            List<SkuHasStockVo> skuHasStockVos = r.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            if(!CollectionUtils.isEmpty(skuHasStockVos)){
                Map<Long, Boolean> collect = skuHasStockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setHasStockMap(collect);
            }
        },executor);
        //购物车项

        //查询用户积分
        confirmVo.setIntegration(memberRespVo.getIntegration());

        //其他数据在Getter中计算

        CompletableFuture.allOf(addressTask,cartItemsTask).get();

        return confirmVo;
    }

}