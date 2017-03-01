package com.arto.sample.service;

import com.arto.core.annotation.Consumer;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MessageRecord;
import com.arto.sample.domain.OrderDO;
import org.springframework.stereotype.Service;

/**
 * Created by xiong.j on 2017/2/20.
 */
@Service
public class ConsumerService {

    @Consumer(destination = "pegesa-test", priority = MessagePriorityEnum.HIGH)
    public void consumerHigh(MessageRecord<OrderDO> message){
        // System.out.println("ConsumerService.consumerHigh message:" + message);
    }

    @Consumer(destination = "pegesa-test-medium", priority = MessagePriorityEnum.MEDIUM)
    public void consumerMedium(MessageRecord<OrderDO> message){
        // System.out.println("ConsumerService.consumerMedium message:" + message);
    }

    @Consumer(destination = "pegesa-test-low", priority = MessagePriorityEnum.LOW)
    public void consumerLow(MessageRecord<OrderDO> message){
        // System.out.println("ConsumerService.consumerLow message:" + message);
    }
}
