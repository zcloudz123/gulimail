package com.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.gulimall.common.to.mq.OrderTo;
import com.gulimall.common.to.mq.StockDetailTo;
import com.gulimall.common.to.mq.StockLockedTo;
import com.gulimall.common.utils.R;
import com.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.gulimall.ware.entity.WareOrderTaskEntity;
import com.gulimall.ware.service.WareSkuService;
import com.gulimall.ware.vo.OrderVo;
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
 * @date 2020-06-28-11:20
 */
@RabbitListener(queues = "stock.release.stock.queue")
@Component
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;


    //根据MQ延时队列解锁库存
    @RabbitHandler
    public void releaseLockStock(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存消息");
        try {
            wareSkuService.unlockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handlerOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭，准备解锁库存");
        try {
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
