package com.gulimall.gulimallcart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gulimall.common.utils.R;
import com.gulimall.gulimallcart.feign.ProductFeignService;
import com.gulimall.gulimallcart.interceptor.CartInterceptor;
import com.gulimall.gulimallcart.service.CartService;
import com.gulimall.gulimallcart.vo.Cart;
import com.gulimall.gulimallcart.vo.CartItem;
import com.gulimall.gulimallcart.vo.SkuInfoVo;
import com.gulimall.gulimallcart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-22-9:48
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer count) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = new CartItem();
        String s = (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(s)){
            //购物车无此商品
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //查询skuInfo
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setImg(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setCount(count);
            }, executor);

            CompletableFuture<Void> getSkuAttrTask = CompletableFuture.runAsync(() -> {
                //查询skuAttr
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuAttrTask,getSkuInfoTask).get();
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }else {
            CartItem updateCartItem = JSON.parseObject(s, CartItem.class);
            updateCartItem.setCount(updateCartItem.getCount() + count);
            cartOps.put(skuId.toString(),JSON.toJSONString(updateCartItem));
            return updateCartItem;
        }

    }

    @Override
    public CartItem getCartItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(s,CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null){
            //已登录(需合并离线购物车)
            List<CartItem> tempCartItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            for (CartItem cartItem :
                    tempCartItems) {
                addToCart(cartItem.getSkuId(),cartItem.getCount());
            }
            //清空临时购物车的数据
            clearCart(CART_PREFIX + userInfoTo.getUserKey());

            Cart cart = new Cart();
            List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserId());
            cart.setItems(cartItems);
            return cart;

        }else{
            //没登录
            Cart cart = new Cart();
            String userKey = CART_PREFIX + userInfoTo.getUserKey();
            cart.setItems(getCartItems(userKey));
            return cart;
        }
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItemBySkuId(skuId);
        cartItem.setCheck(check == 1);
        getCartOps().put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItemBySkuId(skuId);
        if(num != 0){
            cartItem.setCount(num);
        }else{
            getCartOps().delete(skuId.toString());
        }
        getCartOps().put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        getCartOps().delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null) {
            return getCartItems(CART_PREFIX + userInfoTo.getUserId()).stream()
                    .filter(CartItem::getCheck)
                    .map(item->{
                        //更新价格
                        item.setPrice(productFeignService.getPrice(item.getSkuId()));
                        return item;
                    })
                    .collect(Collectors.toList());
        }else{
            return null;
        }
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if(!CollectionUtils.isEmpty(values)){
            return values.stream().map(o -> JSON.parseObject((String) o, CartItem.class)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    //获取redis中需要操作的hash表
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        //判断是否是登录用户
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        return redisTemplate.boundHashOps(cartKey);
    }

    //清空购物车


}
