package com.arto.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.MessageRecord;
import com.arto.core.exception.MqClientException;
import com.arto.event.build.EventListener;
import com.arto.event.service.EventAdviceService;
import com.arto.event.storage.EventInfo;
import com.arto.event.util.TypeReferenceUtil;
import com.arto.kafka.common.Constants;
import com.arto.kafka.common.KUtil;
import com.arto.kafka.consumer.KafkaMessageConsumer;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.event.KafkaConsumeEvent;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 持久化消息待消费事件监听器
 * TODO 需考虑切换消息中件间后持久化消息的处理
 *
 * Created by xiong.j on 2017/1/19.
 */
@Slf4j
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

    @SuppressWarnings("unchecked")
    private void onMessage(KafkaConsumeEvent event) throws Throwable {
        // 持久化信息
        EventInfo eventInfo = event.getEventContext().getEventInfo();
        // 获取主题的配置
        KafkaConsumerConfig config;
        try {
            config = consumer.getConfig(KUtil.extractTopic(eventInfo.getBusinessId()));
        } catch (Throwable t) {
            throw new MqClientException("Can't get consumer config of topic=" + event.getDestination(), t);
        }
        // 反序列化消息
        MessageRecord message;
        try {
            message = JSON.parseObject(eventInfo.getPayload(), TypeReferenceUtil.getType(config.getListener()));
        } catch (Throwable t) {
            throw new MqClientException("Deserializer message failed, message=" + event.getPayload(), t);
        }
        // 设置消息ID
        message.setMessageId(eventInfo.getBusinessId());
        // 重复消费判断
        if (!config.getListener().checkRedeliver(message)) {
            // 消费消息
            config.getListener().onMessage(message);
        } else {
            log.debug("Redelivered message, discard it. message:" + message);
        }
    }
}
