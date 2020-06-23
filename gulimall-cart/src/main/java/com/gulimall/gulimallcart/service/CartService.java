package com.gulimall.gulimallcart.service;

import com.gulimall.gulimallcart.vo.Cart;
import com.gulimall.gulimallcart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-22-9:48
 */
public interface CartService {

    CartItem addToCart(Long skuId, Integer count) throws ExecutionException, InterruptedException;

    CartItem getCartItemBySkuId(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);
}
