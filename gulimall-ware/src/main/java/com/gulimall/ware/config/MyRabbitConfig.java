package com.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-23-13:51
 */
@Configuration
public class MyRabbitConfig {

//    @Autowired
//    RabbitTemplate rabbitTemplate;

    //使用json序列化器
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    //延迟队列，无消费者，死信转发至真-路由队列
    @Bean
    public Queue stockDelayQueue(){
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release.stock");
        arguments.put("x-message-ttl",120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    //真-路由队列
    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    //统筹交换机
    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true,false);
    }

    //交换机与延迟队列的绑定关系
    @Bean
    public Binding stockCreateStockBinding(){
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.create.stock",new HashMap<>());
    }

    //交换机与真-路由队列的绑定关系
    @Bean
    public Binding stockReleaseStockBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.release.stock",new HashMap<>());
    }

    //定制RabbitTemplate
//    @PostConstruct
//    public void initRabbitTemplate(){
//        //消息抵达Broker的确认回调
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                System.out.println("correlationData:" + correlationData);
//                System.out.println("ack:" + ack);
//                System.out.println("cause:" + cause);
//            }
//        });
//        //消息抵达队列的确认回调（失败才会收集）
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//                System.out.println("message:" + message);
//                System.out.println("replyCode:" + replyCode);
//                System.out.println("replyText:" + replyText);
//                System.out.println("exchange:" + exchange);
//                System.out.println("routingKey:" + routingKey);
//            }
//        });
//    }
}
