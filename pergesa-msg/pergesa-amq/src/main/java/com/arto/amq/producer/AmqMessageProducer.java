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
package com.arto.amq.producer;

import com.arto.amq.bootstrap.AmqJmsTemplate;
import com.arto.amq.bootstrap.AmqSpringRegister;
import com.arto.amq.config.AmqConfigManager;
import com.arto.amq.event.AmqProduceEvent;
import com.arto.amq.util.AmqUtil;
import com.arto.core.exception.MqClientException;
import com.arto.event.util.SpringThreadPoolUtil;
import com.arto.event.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.concurrent.ExecutorService;

/**
 * Created by xiong.j on 2017/3/22.
 */
@Slf4j
@Component
public class AmqMessageProducer {

    @Autowired
    @Qualifier("amqJmsTemplate")
    private AmqJmsTemplate jmsTemplate;

    @Autowired
    private AmqSpringRegister connectionFactory;

    /** 消息发送线程池 */
    private volatile ExecutorService executor;

    /** activemq发送的消息最大容量，默认10M */
    private int messageMaxSize = AmqConfigManager.getInt("activemq.message.maxSize", 10485760);

    /**
     * 发送消息
     *
     * @param event
     * @throws MqClientException
     */
    public void send(final AmqProduceEvent event) throws MqClientException{
        innerSend(null, event);
    }

    /**
     * 发送消息
     *
     * @param destination
     * @param event
     * @throws MqClientException
     */
    public void send(final Destination destination, final AmqProduceEvent event) throws MqClientException{
        innerSend(destination, event);
    }

    private void innerSend(final Destination destination, final AmqProduceEvent event) throws MqClientException {
        if (event.getPriority() == 3) {
            // 异步发送, 被SpingJMS封装，无法使用回调(AsyncCallback)
            getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        doSend(destination, event);
                    } catch (Throwable e) {
                        log.error("Send message failed. message event=" + event);
                    }
                }
            });
        } else {
            // 同步发送
            doSend(destination, event);
        }
    }

    private void doSend(final Destination destination, final AmqProduceEvent event){
        try {
            final String message = StringUtil.checkSize(event.getPayload(), messageMaxSize);

            int priority = AmqUtil.convert2AmqPriority(event.getPriority());
            if (destination != null) {
                jmsTemplate.send(destination, new TextMessage(message)
                        , event.getDeliveryMode(), priority, event.getTimeToLive());
            } else {
                jmsTemplate.send(AmqUtil.getDestName(event.getDestination())
                        , new TextMessage(message), event.getDeliveryMode(), priority, event.getTimeToLive());
            }
        } catch (Throwable e) {
            throw new MqClientException(e);
        }
    }

    private class TextMessage implements MessageCreator {

        private String message;

        public TextMessage(final String message) {
            this.message = message;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            return session.createTextMessage(message);
        }
    }

    private ExecutorService getExecutor(){
        if (executor == null){
            synchronized (AmqMessageProducer.class){
                if (executor == null) {
                    executor = SpringThreadPoolUtil.getNewPool("AmqAsyncSend"
                            , AmqConfigManager.getInt("amq.producer.pool.coreSize", 3)
                            , AmqConfigManager.getInt("amq.producer.pool.maxSize", 6)
                            , AmqConfigManager.getInt("amq.producer.pool.queueCapacity", 999)
                            , null).getThreadPoolExecutor();
                }
                return executor;
            }
        }
        return executor;
    }
}
