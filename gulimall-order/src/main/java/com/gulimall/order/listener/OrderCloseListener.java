package com.gulimall.order.listener;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
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
 * @date 2020-06-28-13:14
 */
@RabbitListener(queues = "order.release.delay.queue")
@Component
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        try {
            orderService.closeOrder(orderEntity);
            //手动调用支付宝收单
            System.out.println("手动收单" + orderEntity.getOrderSn());
            AlipayTradeCloseResponse response = Factory.Payment.Common().close(orderEntity.getOrderSn());
//            AlipayTradeQueryResponse response = Factory.Payment.Common().optional("trade_no",null).query(orderEntity.getOrderSn());
            System.out.println("code:" + response.code);
            System.out.println("msg:" + response.msg);
            System.out.println("subCode:" + response.subCode);
            System.out.println("subMsg:" + response.subMsg);
            System.out.println("trade_no:" + response.tradeNo);
            System.out.println("out_trade_no:" + response.outTradeNo);
//            System.out.println("tradeStatus:" + response.tradeStatus);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
