package com.arto.core.common;

/**
 * Created by xiong.j on 2016/7/26.
 */
public interface Constants {

    /** 事件标识"消息中间件" */
    String MQ = "mq";

    /** 消息中间件kafka */
    String KAFKA = "kafka";

    /** 消息中间件activemq */
    String ACTIVEMQ = "activemq";

    /** 消息中间件kafka事件类 */
    String KAFKA_EVENT_BEAN = "com.arto.event.KafkaEvent";

    /** 消息中间件activemq事件类 */
    String ACTIVEMQ_EVENT_BEAN = "com.arto.event.ActivemqEvent";

}