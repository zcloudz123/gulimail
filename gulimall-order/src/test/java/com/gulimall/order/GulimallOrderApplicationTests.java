package com.gulimall.order;

import com.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sendMessage() {
        OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
        orderReturnReasonEntity.setId(1L);
        orderReturnReasonEntity.setCreateTime(new Date());
        orderReturnReasonEntity.setName("hehe");
        orderReturnReasonEntity.setStatus(1);
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity);
        log.info("对象消息发送创建成功");
    }

    @Test
    void name() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange创建成功");
    }

    @Test
    void createQueue() {
        amqpAdmin.declareQueue(new Queue("hello-java-queue",true,false,false));
        log.info("Queue创建成功");
    }

    //String destination, DestinationType destinationType, String exchange, String routingKey,
    //			@Nullable Map<String, Object> arguments
    @Test
    void bind() {
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,"hello-java-exchange","hello.java",new HashMap<>());
        amqpAdmin.declareBinding(binding);
        log.info("Binding创建成功");
    }

    @Test
    void contextLoads() {
    }

}
