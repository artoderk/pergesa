package com.arto.sample.service;

import com.arto.core.annotation.Consumer;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.exception.MqClientException;
import com.arto.sample.dao.OrderDao;
import com.arto.sample.domain.OrderDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Created by xiong.j on 2017/2/20.
 */
@Service
public class AmqConsumerService {

    @Autowired
    private OrderDao dao;

    private static Random random = new Random();

    @Consumer(type = MqTypeEnum.ACTIVEMQ, destination = "q-test", priority = MessagePriorityEnum.HIGH, checkRedeliver = "checkRedeliver")
    public void consumerHigh(MessageRecord<OrderDO> message){
        System.out.println("ConsumerService.consumerHigh message:" + message);
        throw new MqClientException("test exception.");
    }

    @Consumer(type = MqTypeEnum.ACTIVEMQ, destination = "q-test-medium", priority = MessagePriorityEnum.MEDIUM)
        public void consumerMedium(MessageRecord<OrderDO> message){
        System.out.println("ConsumerService.consumerMedium message:" + message);
    }

    @Consumer(type = MqTypeEnum.ACTIVEMQ, destination = "q-test-low", priority = MessagePriorityEnum.LOW)
    public void consumerLow(MessageRecord<OrderDO> message){
        System.out.println("ConsumerService.consumerLow message:" + message);
    }

    public boolean checkRedeliver(MessageRecord<OrderDO> message){
        boolean redeliver = false;
        if (random.nextInt(100) == 50) {
            redeliver = true;
        }
        System.out.println("ConsumerService.checkRedeliver " + redeliver + ", message:" + message);

        return redeliver; // true: 重复消息 false: 非重复消息
    }
}
