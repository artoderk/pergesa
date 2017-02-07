package com.arto.kafka.executor;

import com.arto.event.build.EventBusFactory;
import com.arto.kafka.event.KafkaConsumeEvent;
import com.arto.kafka.event.KafkaEvent;
import com.arto.kafka.listener.KafkaConsumeEventListener;
import com.arto.kafka.listener.KafkaEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by xiong.j on 2017/1/6.
 */
@Component
public class KafkaEventExecutor {

    /** 消息需发送事件监听器 */
    @Autowired
    private KafkaEventListener kafkaEventListener;

    /** 消息需消费事件监听器 */
    @Autowired
    private KafkaConsumeEventListener kafkaConsumeEventListener;

    @PostConstruct
    public void init() throws Exception {
        // 注册事件
        EventBusFactory.getInstance().register(KafkaEvent.class, kafkaEventListener);
        EventBusFactory.getInstance().register(KafkaConsumeEvent.class, kafkaConsumeEventListener);
    }
}
