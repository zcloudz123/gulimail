package com.gulimall.order.config;

import com.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @decription:Spring自动创建声明的队列，交换机
 * @author: zyy
 * @date 2020-06-27-11:46
 */
@Configuration
public class MyMQConfig {

    //String name, boolean durable, boolean exclusive, boolean autoDelete,
    //			@Nullable Map<String, Object> arguments
    @Bean
    public Queue orderDelayQueue(){
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.delay.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",new HashMap<>());
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",new HashMap<>());
    }

    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.other.#",new HashMap<>());
    }
}
