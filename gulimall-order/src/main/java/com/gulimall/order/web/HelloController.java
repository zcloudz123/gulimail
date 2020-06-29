package com.gulimall.order.web;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.gulimall.order.entity.OrderEntity;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-23-16:12
 */
@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;


    @ResponseBody
    @GetMapping("/test/alipay")
    public String testAliEasyPay() throws Exception {
        AlipayTradePagePayResponse response = Factory.Payment.Page().pay("测试支付", "20200628", "88", "http://3mcbrx.natappfree.cc/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp");
        return response.body;
    }

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);
        return "ok";
    }

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }
}
