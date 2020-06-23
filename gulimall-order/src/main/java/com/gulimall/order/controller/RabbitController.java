package com.gulimall.order.controller;

import com.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-23-15:04
 */
@Slf4j
@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMsg(){
        OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
        orderReturnReasonEntity.setId(1L);
        orderReturnReasonEntity.setCreateTime(new Date());
        orderReturnReasonEntity.setName("hehe");
        orderReturnReasonEntity.setStatus(1);
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity,new CorrelationData(UUID.randomUUID().toString()));
        log.info("对象消息发送创建成功");

        return "ok";
    }
}
