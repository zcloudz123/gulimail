package com.gulimall.order.web;

import com.gulimall.common.exception.NoStockException;
import com.gulimall.order.service.OrderService;
import com.gulimall.order.vo.OrderConfirmVo;
import com.gulimall.order.vo.OrderSubmitVo;
import com.gulimall.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-23-17:17
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirm",confirmVo);

        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes){

        SubmitOrderRespVo submitOrderRespVo = null;
        try {
            submitOrderRespVo = orderService.submitOrder(orderSubmitVo);
        } catch (NoStockException e) {
            redirectAttributes.addFlashAttribute("msg","库存不足");
            return "redirect:http://order.gulimall.com/toTrade";
        } catch (Exception e){
            return "redirect:http://order.gulimall.com/toTrade";
        }
        if(submitOrderRespVo.getCode() == 0){
            //下单成功跳转至支付选择页
            model.addAttribute("submitOrderRespVo",submitOrderRespVo);
            return "pay";
        }else{
            //下单失败返回订单确认页
            String msg = "";
            switch (submitOrderRespVo.getCode()){
                case 1:
                    msg += "订单信息过期，请重新提交订单";
                    break;
                case 2:
                    msg += "订单商品价格发生变化，请重新确认订单";
                    break;
                case 3:
                    msg += "库存不足";
                    break;
                default:
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
