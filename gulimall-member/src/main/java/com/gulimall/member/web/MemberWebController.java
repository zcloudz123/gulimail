package com.gulimall.member.web;

import com.gulimall.common.utils.R;
import com.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-28-17:57
 */
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String MemberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") String pageNum, Model model){

        //可以获取到支付宝传来的请求数据
        //主要是验证签名

        Map<String,Object> params = new HashMap<>();
        params.put("page",pageNum);
        R r = orderFeignService.listWithItems(params);
        model.addAttribute("orders",r);
        return "orderList";
    }
}
