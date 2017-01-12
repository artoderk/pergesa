package com.arto.kafka.executor;

import com.arto.event.common.Constants;
import com.arto.event.build.EventBusFactory;
import com.arto.kafka.listener.KafkaEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by xiong.j on 2017/1/6.
 */

@Component
public class KafkaEventExecutor {

    @Autowired
    private KafkaEventListener kafkaEventListener;

    @PostConstruct
    public void init() throws Exception {
        EventBusFactory.getInstance().register(Constants.MQ, kafkaEventListener);
    }
}
