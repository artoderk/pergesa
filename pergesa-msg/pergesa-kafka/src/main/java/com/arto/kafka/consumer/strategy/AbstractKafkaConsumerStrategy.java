package com.arto.kafka.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.MessageRecord;
import com.arto.core.exception.MqClientException;
import com.arto.event.util.TypeReferenceUtil;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.google.common.base.Strings;

import java.lang.reflect.Method;

/**
 * TODO 使用注解形式消费时用反射性能低下，需转成静态代理
 *
 * Created by xiong.j on 2017/2/21.
 */
public class AbstractKafkaConsumerStrategy {

    /**
     * 反序列化消息
     *
     * @param config
     * @param payload
     * @return
     */
    protected MessageRecord deserializerMessage(KafkaConsumerConfig config, String payload) {
        MessageRecord messageRecord;
        try {
            if (config.getListener() != null) {
                // 通过接口消费消息
                messageRecord = JSON.parseObject(payload, TypeReferenceUtil.getType(config.getListener()));
            } else {
                // 通过注解消费消息
                messageRecord = JSON.parseObject(payload, TypeReferenceUtil.getType(config.getBean(), config.getMethod().getName()));
            }
        } catch (Throwable t) {
            throw new MqClientException("Deserializer message failed, message=" + payload + ", config=" + config, t);
        }

        if (messageRecord == null || messageRecord.getMessage() == null) {
            throw new MqClientException("Deserializer message failed, message=" + payload + ", config=" + config);
        }
        return messageRecord;
    }

    /**
     * 重复消费检测
     *
     * @param config
     * @param message
     * @return
     */
    @SuppressWarnings("unchecked")
    protected boolean checkRedeliver(KafkaConsumerConfig config, MessageRecord message) {
        try {
            if (config.getListener() != null) {
                // 通过接口消费消息
                return config.getListener().checkRedeliver(message);
            } else if (!Strings.isNullOrEmpty(config.getCheckRedeliver())) {
                // 通过注解消费消息
                Method method = config.getBean().getClass().getDeclaredMethod(config.getCheckRedeliver(), config.getMethod().getParameterTypes());
                if (!method.getParameterTypes()[0].equals(MessageRecord.class)) {
                    throw new MqClientException("@Consumer method [" + method + "] should only to be MessageRecord<?> ");
                }
                return (Boolean) method.invoke(config.getBean(), message);
            } else {
                return false;
            }
        } catch (Throwable t) {
            throw new MqClientException("Check redeliver failed, message=" + message, t);
        }
    }

    /**
     * 消费消息
     *
     * @param config
     * @param message
     */
    @SuppressWarnings("unchecked")
    protected void onMessage(KafkaConsumerConfig config, MessageRecord message){
        try {
            if (config.getListener() != null) {
                // 通过接口消费消息
                config.getListener().onMessage(message);
            } else {
                // 通过注解消费消息
                config.getMethod().invoke(config.getBean(), message);
            }
        } catch (Throwable t) {
            throw new MqClientException("Consume message failed, message=" + message, t);
        }
    }
}
