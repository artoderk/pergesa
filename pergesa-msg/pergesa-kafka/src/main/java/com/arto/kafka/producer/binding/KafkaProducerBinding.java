package com.arto.kafka.producer.binding;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.Constants;
import com.arto.core.producer.MqProducer;
import com.arto.event.build.EventBusFactory;
import com.arto.kafka.event.KafkaEvent;
import lombok.RequiredArgsConstructor;

/**
 * Created by xiong.j on 2017/1/12.
 */
@RequiredArgsConstructor
public class KafkaProducerBinding implements MqProducer {

    private final KafkaProducerConfig config;

    @Override
    public void send(Object message) throws Exception {
        KafkaEvent event = buildEvent(null, -1, message);

        EventBusFactory.getInstance().post(event);
    }

    @Override
    public void send(String key, Object message) throws Exception {
        KafkaEvent event = buildEvent(key, -1, message);
    }

    @Override
    public void send(String key, int partition, Object message) throws Exception {
        KafkaEvent event = buildEvent(key, partition, message);
    }

    private KafkaEvent buildEvent(String key, int partition, Object message){
        KafkaEvent event = new KafkaEvent();
        // 对应Topic
        event.setDestination(config.getDestination());
        // 对应Acks
        event.setPriority(config.getPriority());
        // 事件分组
        event.setGroup(Constants.MQ);
        // Hash主键
        event.setKey(key);
        // 分区
        event.setPartition(partition);
        // 回调
        event.setCallback(config.getCallback());
        // 消息 使用fastjson序列化
        event.setPayload(JSON.toJSONString(message));
        return event;
    }
}
