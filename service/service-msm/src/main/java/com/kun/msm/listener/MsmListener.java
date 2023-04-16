package com.kun.msm.listener;

import com.kun.common.constant.MqConst;
import com.kun.msm.service.MsmService;
import com.kun.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author jiakun
 * @create 2023-03-13-17:39
 */
@Component
public class MsmListener {
    @Autowired
    private MsmService msmService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo, Message message, Channel channel) throws Exception {
        msmService.sendNotice(msmVo);
    }




}
