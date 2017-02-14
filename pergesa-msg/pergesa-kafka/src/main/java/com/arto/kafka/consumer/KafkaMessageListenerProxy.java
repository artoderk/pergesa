package com.arto.kafka.consumer;

import com.arto.core.consumer.MqListener;

/**
 * Created by xiong.j on 2017/1/22.
 */
public interface KafkaMessageListenerProxy {

    void onMessage(MqListener listener, String type, String message);
}
