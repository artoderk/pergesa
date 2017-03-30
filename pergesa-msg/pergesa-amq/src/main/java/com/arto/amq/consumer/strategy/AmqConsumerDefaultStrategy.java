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
package com.arto.amq.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.arto.amq.common.AmqConstants;
import com.arto.amq.consumer.binding.AmqConsumerConfig;
import com.arto.amq.event.AmqConsumeEvent;
import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.consumer.strategy.AbstractConsumerStrategy;
import com.arto.core.exception.MqClientException;
import com.arto.event.bootstrap.Event;
import com.arto.event.common.Destroyable;
import com.arto.event.service.PersistentEventService;
import com.arto.event.util.SpringContextHolder;
import com.arto.event.util.SpringDestroyableUtil;
import com.arto.event.util.ThreadUtil;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认消费模式，适合重要消息
 * (单条消息处理完成后消费标识同步提交，为了避免阻塞后续消息，消息处理出错 > 3次后该消息入库，等待调度任务重试处理)
 *
 * Created by xiong.j on 2017/3/29.
 */
@Slf4j
class AmqConsumerDefaultStrategy extends AbstractConsumerStrategy implements AmqConsumerStrategy, Destroyable {

    private final PersistentEventService service;

    /** 消息拉取线程关闭Flag */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    AmqConsumerDefaultStrategy(){
        this.service = SpringContextHolder.getBean("persistentEventService");
        // 注册勾子
        SpringDestroyableUtil.add("kafkaConsumerDefaultStrategy", this);
    }

    @Override
    public void onMessage(final AmqConsumerConfig config, final TextMessage message) throws JMSException {
        tryConsume(config, message);
    }

    /**
     * 销毁
     */
    @Override
    public void destroy() {
        closeFlag.set(true);
        log.info("Destroy KafkaConsumerDefaultStrategy successful.");
    }

    @SuppressWarnings("unchecked")
    private void tryConsume(AmqConsumerConfig config, TextMessage textMessage) throws JMSException {
        MessageRecord message = null;
        try {
            // 反序列化消息
            message = deserializerMessage(config, textMessage.getText());
        } catch (Throwable e) {
            log.warn("Deserializer record failed, message:" + textMessage, e);
            // 持久化消息，以便重试
            infiniteRetry(textMessage, null);
        }

        // 如果消费出错，重试消费3次，超过三次持久化后由调度任务再重试
        for (int i = 1; i <= 3; i++) {
            try {
                // 重复消费检测
                if (!checkRedeliver(config, message)) {
                    // 消费消息
                    onMessage(config, message);
                } else {
                    log.info("Check redeliver is true, discard this message:" + message);
                }
                break;
            } catch (Throwable e) {
                log.warn("Receive message failed, waiting for retry. record:" + textMessage, e);
                if (i == 3) {
                    // 持久化消息，以便重试
                    infiniteRetry(textMessage, message);
                } else {
                    // 消息处理错误，暂停处理一小会
                    ThreadUtil.sleep(5000, log);
                }
            }
        }
    }

    private void infiniteRetry(final TextMessage textMessage
            , MessageRecord message) throws JMSException {
        boolean failed = true;
        // 转换为事件
        Event event = buildEvent(textMessage, message);
        // 无限重试直到持久化成功
        while (!closeFlag.get()){
            try {
                service.persist(event, AmqConstants.A_CONSUME_EVENT_BEAN);
                failed = false;
                break;
            } catch (Throwable e) {
                log.warn("Persist message failed, waiting for retry. message:" + textMessage, e);
            }
            // 持久化消息错误，暂停处理一小会
            ThreadUtil.sleep(5000, log);
        }
        if (failed) {
            throw new MqClientException("Persist message failed when stop server.");
        } else {
            log.warn("Persisted message to db waiting for retry. message:" + textMessage);
        }
    }

    @SuppressWarnings("unchecked")
    private Event buildEvent(final TextMessage textMessage, MessageRecord message) throws JMSException {
        // 生成事件
        AmqConsumeEvent event = new AmqConsumeEvent();
        // 事件分组
        event.setGroup(AmqConsumeEvent.class);
        // 默认业务类型
        event.setBusinessType(AmqConstants.A_CONSUME);
        // 消息ID
        event.setMessageId(textMessage.getJMSMessageID());
        // 消息
        event.setPayload(message);
        // 目的地
        event.setDestination(((ActiveMQDestination)textMessage.getJMSDestination()).getPhysicalName());
        // 消息类型
        event.setType(MqTypeEnum.ACTIVEMQ.getMemo());
        // 是否持久化
        event.setPersistent(true);
        if (message == null) {
            JSONObject jsonObject = JSON.parseObject(textMessage.getText());
            if (!Strings.isNullOrEmpty(jsonObject.getString("businessId"))) {
                // 消息自带业务流水号
                event.setBusinessId(jsonObject.getString("businessId"));
                // 消息自带业务类型
                event.setBusinessType(jsonObject.getString("businessType"));
            } else {
                // 业务流水号为消息ID
                event.setBusinessId(textMessage.getJMSMessageID());
            }
            // 消息解析出错时消息设为json对象
            event.setPayload(jsonObject);
        } else {
            // 设置消息Id
            event.setMessageId(message.getMessageId());
            if (Strings.isNullOrEmpty(message.getBusinessId())) {
                // 以非事务消息发送时业务流水号为消息ID
                event.setBusinessId(message.getMessageId());
            } else {
                // 消息自带业务流水号
                event.setBusinessId(message.getBusinessId());
                // 消息自带业务类型
                event.setBusinessType(message.getBusinessType());
            }
        }
        return event;
    }


}
