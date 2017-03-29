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
package com.arto.kafka.listener;

import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.consumer.strategy.AbstractConsumerStrategy;
import com.arto.core.exception.MqClientException;
import com.arto.event.bootstrap.EventListener;
import com.arto.event.service.EventAdviceService;
import com.arto.event.storage.EventInfo;
import com.arto.kafka.bootstrap.KafkaClientFactory;
import com.arto.kafka.common.Constants;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.event.KafkaConsumeEvent;
import com.google.common.base.Strings;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 持久化消息待消费事件监听器
 *
 * Created by xiong.j on 2017/1/19.
 */
@Slf4j
@Component
public class KafkaConsumeEventListener extends AbstractConsumerStrategy implements EventListener<KafkaConsumeEvent> {

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
        // 获取主题的配置
        KafkaConsumerConfig config = getConsumerConfig(event.getDestination());
        // 反序列化消息
        MessageRecord message = deserializer(config, event);
        // 重复消费判断
        if (!checkRedeliver(config, message)) {
            // 消费消息
            onMessage(config, message);
        } else {
            log.warn("Redelivered message, discard it. message:" + message);
        }
    }

    private KafkaConsumerConfig getConsumerConfig(String destination){
        // 获取Kafka客户端工厂
        KafkaClientFactory clientFactory = (KafkaClientFactory)MqClient.getMqFactory(MqTypeEnum.KAFKA.getMemo());
        try {
            if (clientFactory != null) {
                return clientFactory.getConsumerConfig(destination);
            } else {
                throw new MqClientException("Kafka client not initialzation.");
            }
        } catch (Throwable t) {
            throw new MqClientException("Can't get consumer config of topic:" + destination, t);
        }
    }

    private MessageRecord deserializer(KafkaConsumerConfig config, KafkaConsumeEvent event) {
        // 持久化信息
        EventInfo eventInfo = event.getEventContext().getEventInfo();
        // 反序列化消息
        MessageRecord message = deserializerMessage(config, event.getPayload().toString());
        if (!Strings.isNullOrEmpty(eventInfo.getBusinessId())) {
            message.setBusinessId(eventInfo.getBusinessId());
            message.setBusinessType(eventInfo.getBusinessType());
        }
        message.setMessageId(event.getMessageId());
        return message;
    }
}
