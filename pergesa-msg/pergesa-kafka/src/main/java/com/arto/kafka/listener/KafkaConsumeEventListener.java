package com.arto.kafka.listener;

import com.arto.event.build.EventListener;
import com.arto.event.service.EventAdviceService;
import com.arto.kafka.common.Constants;
import com.arto.kafka.event.KafkaConsumeEvent;
import com.arto.kafka.producer.KafkaMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息需消费事件监听器
 *
 * Created by xiong.j on 2017/1/19.
 */
@Component
public class KafkaConsumeEventListener implements EventListener<KafkaConsumeEvent> {

    @Autowired
    private KafkaMessageProducer producer;

    @Autowired
    private EventAdviceService service;

    @Override
    public void listen(KafkaConsumeEvent event) {
        try {
            // 前处理
            service.before(event);
            // 消费消息
            //producer.send(event);
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
