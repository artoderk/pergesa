package com.arto.kafka.event;

import com.arto.event.bootstrap.EventBusFactory;
import com.arto.kafka.listener.KafkaConsumeEventListener;
import com.arto.kafka.listener.KafkaProduceEventListener;
import com.arto.kafka.listener.KafkaReportEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by xiong.j on 2017/1/6.
 */
@Component
public class KafkaEventManager {

    /** 消息需发送事件监听器 */
    @Autowired
    private KafkaProduceEventListener kafkaProduceEventListener;

    /** 持久件事件报告监听器 */
    @Autowired
    private KafkaReportEventListener kafkaReportEventListener;

    /** 消息需消费事件监听器 */
    @Autowired
    private KafkaConsumeEventListener kafkaConsumeEventListener;

    @PostConstruct
    public void init() throws Exception {
        // 注册事件
        EventBusFactory.getInstance().register(KafkaProduceEvent.class, kafkaProduceEventListener);
        EventBusFactory.getInstance().register(KafkaReportEvent.class, kafkaReportEventListener);
        EventBusFactory.getInstance().register(KafkaConsumeEvent.class, kafkaConsumeEventListener);
    }
}
