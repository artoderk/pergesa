package com.arto.kafka.common;

import com.arto.kafka.event.KafkaConsumeEvent;
import com.arto.kafka.event.KafkaEvent;

/**
 * Created by xiong.j on 2016/7/26.
 */
public interface Constants {

    /** 消息中间件kafka简写 */
    String K = "K";

    /** 消息中间件kafka */
    String KAFKA = "kafka";

    /** 消息中间件kafka发送消息事件类 */
    String KAFKA_EVENT_BEAN = KafkaEvent.class.getName();

    /** 消息中间件kafka消费消息标识 */
    String K_CONSUME = "K02";

    /** 消息中间件kafka消费消息事件类 */
    String K_CONSUME_EVENT_BEAN = KafkaConsumeEvent.class.getName();
}