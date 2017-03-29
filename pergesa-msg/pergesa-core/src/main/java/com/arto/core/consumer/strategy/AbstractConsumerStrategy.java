/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.arto.core.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.ConsumerConfig;
import com.arto.core.exception.MqClientException;
import com.arto.event.util.TypeReferenceUtil;
import com.google.common.base.Strings;

import java.lang.reflect.Method;

/**
 * TODO 使用注解形式消费时用反射性能低下，需转成静态代理
 *
 * Created by xiong.j on 2017/3/29.
 */
public class AbstractConsumerStrategy {

    /**
     * 反序列化消息
     *
     * @param config
     * @param payload
     * @return
     */
    protected MessageRecord deserializerMessage(ConsumerConfig config, String payload) {
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
            throw new MqClientException("Deserializer message failed, message:" + payload + ", config:" + config, t);
        }

        if (messageRecord == null || messageRecord.getMessage() == null) {
            throw new MqClientException("Deserializer message failed, message:" + payload + ", config:" + config);
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
    protected boolean checkRedeliver(ConsumerConfig config, MessageRecord message) {
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
            throw new MqClientException("Check redeliver failed, message:" + message, t);
        }
    }

    /**
     * 消费消息
     *
     * @param config
     * @param message
     */
    @SuppressWarnings("unchecked")
    protected void onMessage(ConsumerConfig config, MessageRecord message){
        try {
            if (config.getListener() != null) {
                // 通过接口消费消息
                config.getListener().onMessage(message);
            } else {
                // 通过注解消费消息
                config.getMethod().invoke(config.getBean(), message);
            }
        } catch (Throwable t) {
            throw new MqClientException("Consume message failed, message:" + message, t);
        }
    }
}
