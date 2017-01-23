package com.arto.kafka.listener;

import com.arto.event.build.EventListener;
import com.arto.event.service.EventAdviceService;
import com.arto.kafka.common.Constants;
import com.arto.kafka.consumer.KMessageListenerProxy;
import com.arto.kafka.consumer.KMessageListenerProxyFactory;
import com.arto.kafka.consumer.KafkaMessageConsumer;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.event.KafkaConsumeEvent;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 持久化消息待消费事件监听器
 * TODO 需考虑切换消息中件间后持久化消息的处理
 *
 * Created by xiong.j on 2017/1/19.
 */
@Component
public class KafkaConsumeEventListener implements EventListener<KafkaConsumeEvent> {

    @Autowired
    private KafkaMessageConsumer consumer;

    @Autowired
    private EventAdviceService service;

    @Subscribe
    @AllowConcurrentEvents
    @Override
    public void listen(KafkaConsumeEvent event) {
        try {
            // 前处理
            service.before(event);
            // 消费消息
            onMessage(event);
            // 后处理
            service.after(event);
        } catch (Throwable e) {
            // 失败处理
            service.fail(event, e);
        }
    }

    @Override
    public String getIdentity() {
        return Constants.K_CONSUME;
    }

    private void onMessage(KafkaConsumeEvent event) {
        KafkaConsumerConfig config = consumer.getConfig(event.getDestination());
        try {
            KMessageListenerProxy proxy = KMessageListenerProxyFactory.getProxy(config.getListener(), config.getType());
            proxy.onMessage(config.getListener(), config.getType(), event.getPayload());
        } catch (Throwable throwable) {
            // TODO
            throwable.printStackTrace();
        }
    }
}
