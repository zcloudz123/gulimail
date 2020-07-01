package com.gulimall.order.listener;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.gulimall.common.to.mq.SeckillOrderTo;
import com.gulimall.order.entity.OrderEntity;
import com.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-30-23:33
 */
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        try {
            //创建秒杀订单的详细信息
            orderService.createSeckillOrder(seckillOrderTo);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
