package com.gulimall.order.listener;

import com.gulimall.order.service.OrderService;
import com.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-28-22:23
 */
@RestController
public class OrderPayedListener {

    @Autowired
    OrderService orderService;

    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo payAsyncVo,HttpServletRequest request){
        Map<String, String[]> map = request.getParameterMap();
        Map<String,String> checkMap = new HashMap<>();
        for (String key : map.keySet()) {
            checkMap.put(key,request.getParameter(key));
        }
        String result;
        try {
            result = orderService.handlePayResult(payAsyncVo,checkMap);
        } catch (Exception e) {
            return "error";
        }
        //支付宝要求返回确认通知
        return result;
    }
}
