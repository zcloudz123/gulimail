package com.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.constant.OrderConstant;
import com.gulimall.common.exception.NoStockException;
import com.gulimall.common.to.SkuHasStockVo;
import com.gulimall.common.to.mq.OrderTo;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;
import com.gulimall.common.utils.R;
import com.gulimall.common.vo.MemberRespVo;
import com.gulimall.order.dao.OrderDao;
import com.gulimall.order.entity.OrderEntity;
import com.gulimall.order.entity.OrderItemEntity;
import com.gulimall.order.enume.OrderStatusEnum;
import com.gulimall.order.feign.CartFeignService;
import com.gulimall.order.feign.MemberFeignService;
import com.gulimall.order.feign.ProductFeignService;
import com.gulimall.order.feign.WareFeignService;
import com.gulimall.order.interceptor.LoginUserInterceptor;
import com.gulimall.order.service.OrderItemService;
import com.gulimall.order.service.OrderService;
import com.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    public ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

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

        //TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(),token,30, TimeUnit.MINUTES);

        CompletableFuture.allOf(addressTask,cartItemsTask).get();

        return confirmVo;
    }

//    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderRespVo submitOrderRespVo = new SubmitOrderRespVo();
        submitOrderRespVo.setCode(0);
        confirmVoThreadLocal.set(orderSubmitVo);
        //验订单token
        String orderToken = orderSubmitVo.getOrderToken();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        //令牌验证和删除必须保证原子性,使用Lua脚本实现
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long ret = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if(ret == 0L){
            submitOrderRespVo.setCode(1);
            return submitOrderRespVo;
        }
        //构建订单及订单项
        OrderCreateTo order = createOrder();
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal payPrice = orderSubmitVo.getPayPrice();
        if (!(Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01)) {
            //验价失败
            submitOrderRespVo.setCode(2);
            return submitOrderRespVo;
        }
        //验价成功，保存订单信息
        saveOrder(order);

        //锁定库存 有异常回滚订单数据
        WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
        wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
        List<OrderItemVo> collect = order.getOrderItems().stream().map(orderItemEntity -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(orderItemEntity.getSkuId());
            orderItemVo.setCount(orderItemEntity.getSkuQuantity());
            orderItemVo.setTitle(orderItemEntity.getSkuName());
            return orderItemVo;
        }).collect(Collectors.toList());
        wareSkuLockVo.setLockItems(collect);
        //远程锁定库存
        R r = wareFeignService.orderLockStock(wareSkuLockVo);
        if(r.getCode() != 0){
            //库存锁定失败
            throw new NoStockException(0L);
//            submitOrderRespVo.setCode(3);
//            return submitOrderRespVo;
        }
        submitOrderRespVo.setOrder(order.getOrder());
        //MQ缓存新建的订单消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
        return submitOrderRespVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn",orderSn));
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity entity = this.getById(orderEntity.getId());
        if(entity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity updateOrderEntity = new OrderEntity();
            updateOrderEntity.setId(entity.getId());
            updateOrderEntity.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrderEntity);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(entity,orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
        }
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getOrderItems();

        orderEntity.setCreateTime(new Date());
        this.save(orderEntity);

        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder(){
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        //生成订单号
        String orderSn = IdWorker.getTimeId();
        //构建订单
        OrderEntity orderEntity = buildOrder(orderSn);
        //构建订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        //计算订单中所有的价格信息
        computePrice(orderEntity,orderItemEntities);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);


        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //统计订单项中的总金额、优惠、打折、成长值、实付金额
        BigDecimal total = new BigDecimal(0);
        BigDecimal couponAmount = new BigDecimal(0);
        BigDecimal integrationAmount = new BigDecimal(0);
        BigDecimal promotionAmount = new BigDecimal(0);
        BigDecimal giftGrowth = new BigDecimal(0);
        BigDecimal giftIntegration = new BigDecimal(0);

        for (OrderItemEntity orderItemEntity :
                orderItemEntities) {
            total = total.add(orderItemEntity.getRealAmount());
            couponAmount = couponAmount.add(orderItemEntity.getCouponAmount());
            integrationAmount = integrationAmount.add(orderItemEntity.getIntegrationAmount());
            promotionAmount = promotionAmount.add(orderItemEntity.getPromotionAmount());
            giftGrowth = giftGrowth.add(new BigDecimal(orderItemEntity.getGiftGrowth()));
            giftIntegration = giftIntegration.add(new BigDecimal(orderItemEntity.getGiftIntegration()));

        }
        orderEntity.setTotalAmount(total);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setGrowth(giftGrowth.intValue());
        orderEntity.setIntegration(giftIntegration.intValue());
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

    }

    //构建订单
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setOrderSn(orderSn);
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = r.getData(new TypeReference<FareVo>() {
        });
        //设置收货地址，运费
        orderEntity.setFreightAmount(fareVo.getFare());
        orderEntity.setReceiverDetailAddress(fareVo.getMemberAddressVo().getDetailAddress());
        orderEntity.setReceiverCity(fareVo.getMemberAddressVo().getCity());
        orderEntity.setReceiverName(fareVo.getMemberAddressVo().getName());
        orderEntity.setReceiverPhone(fareVo.getMemberAddressVo().getPhone());
        orderEntity.setReceiverProvince(fareVo.getMemberAddressVo().getProvince());
        orderEntity.setReceiverRegion(fareVo.getMemberAddressVo().getRegion());
        orderEntity.setReceiverPostCode(fareVo.getMemberAddressVo().getPostCode());
        //设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        return orderEntity;
    }

    //构建订单项
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //获取所有的订单 最后确定购物项的价格
        List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
        if(!CollectionUtils.isEmpty(items)){
            return items.stream().map(orderItemVo -> {
                OrderItemEntity orderItemEntity = buildOrderItem(orderItemVo);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //spu信息
        R r = productFeignService.getSpuInfoBySkuId(orderItemVo.getSkuId());
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        //sku信息
        orderItemEntity.setSkuId(orderItemVo.getSkuId());
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getImg());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttr(),";"));
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());
        //优惠信息

        //积分信息
        orderItemEntity.setGiftGrowth(orderItemVo.getPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity())).intValue());
        orderItemEntity.setGiftIntegration(orderItemVo.getPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity())).intValue());

        //订单项的价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal(0));
        orderItemEntity.setCouponAmount(new BigDecimal(0));
        orderItemEntity.setIntegrationAmount(new BigDecimal(0));
        BigDecimal origin = orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount()));
        orderItemEntity.setRealAmount(origin
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount()));

        return orderItemEntity;
    }

}