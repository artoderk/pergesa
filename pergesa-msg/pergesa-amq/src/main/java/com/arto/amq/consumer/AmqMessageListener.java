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
package com.arto.amq.consumer;

import com.arto.amq.consumer.binding.AmqConsumerConfig;
import com.arto.amq.consumer.strategy.AmqConsumerStrategyFactory;
import com.arto.core.exception.MqClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * Created by xiong.j on 2017/3/28.
 */
@Slf4j
public class AmqMessageListener {

    private AmqMessageConsumer messageConsumer;

    /**
     * 异步消费模式，由MessageListenerContainer调用
     *
     * @param message
     * @throws Exception
     */
    public void handleMessage(TextMessage message) throws Exception{
        try {
            String destName = ((ActiveMQDestination)message.getJMSDestination()).getPhysicalName();
            AmqConsumerConfig config = messageConsumer.getDestConfig(destName);
            AmqConsumerStrategyFactory.getInstance().getStrategy(message.getJMSPriority()).onMessage(config, message);
        } catch (JMSException e) {
            log.error("Consume message failed.", e);
            throw e;
        } catch (MqClientException e) {
            throw new Exception(e);
        }
    }

    public void setMessageConsumer(AmqMessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }
}
