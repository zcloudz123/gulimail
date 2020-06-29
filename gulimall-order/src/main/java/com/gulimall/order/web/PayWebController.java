package com.gulimall.order.web;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.gulimall.order.service.OrderService;
import com.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-28-17:28
 */
@Controller
public class PayWebController {

    @Autowired
    OrderService orderService;

    //直接将支付页面返回
    @ResponseBody
    @GetMapping("payOrder")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws Exception {
        PayVo payVo = orderService.getOrderPay(orderSn);
        AlipayTradePagePayResponse response = Factory.Payment.Page().optional("timeout_express","1m").pay(payVo.getSubject(), payVo.getOut_trade_no(), payVo.getTotal_amount(), payVo.getReturnUrl());
        return response.body;
    }


}
