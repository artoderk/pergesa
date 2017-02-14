package com.arto.kafka.listener;

import com.arto.event.bootstrap.EventListener;
import com.arto.event.service.EventAdviceService;
import com.arto.kafka.common.Constants;
import com.arto.kafka.event.KafkaProduceEvent;
import com.arto.kafka.producer.KafkaMessageProducer;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息发送事件
 * TODO 需考虑切换消息中件间后持久化消息的处理
 *
 * Created by xiong.j on 2017/1/6.
 */
@Component
public class KafkaProduceEventListener implements EventListener<KafkaProduceEvent> {

    @Autowired
    private KafkaMessageProducer producer;

    @Autowired
    private EventAdviceService service;

    @Subscribe
    @AllowConcurrentEvents
    @Override
    public void listen(KafkaProduceEvent event) {
        try {
            // 前处理
            service.before(event);
            // 发送消息
            producer.send(event);
            // 后处理
            service.after(event);
        } catch (Throwable e) {
            // 失败处理
            service.fail(event, e);
        }
    }

    @Override
    public String getIdentity() {
        return Constants.KAFKA;
    }

}
