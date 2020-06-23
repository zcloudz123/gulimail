package com.gulimall.gulimallcart.controller;

import com.gulimall.gulimallcart.service.CartService;
import com.gulimall.gulimallcart.vo.Cart;
import com.gulimall.gulimallcart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-22-9:51
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;


    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        cartService.countItem(skuId,num);

        return "redirect:http://cart.gulimall.com/cart.html";
   }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 使用user-key这个Cookie去标识浏览器/临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(
            @RequestParam("skuId") Long skuId,
            @RequestParam("count") Integer count,
            RedirectAttributes redirectAttributes
    ) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,count);
        redirectAttributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        //再次查询购物车数据
        CartItem cartItem = cartService.getCartItemBySkuId(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }
}
