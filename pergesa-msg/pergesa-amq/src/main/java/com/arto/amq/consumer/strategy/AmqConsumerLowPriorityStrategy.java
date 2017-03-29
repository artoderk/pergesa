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

import com.arto.amq.consumer.binding.AmqConsumerConfig;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.strategy.AbstractConsumerStrategy;
import lombok.extern.slf4j.Slf4j;

import javax.jms.TextMessage;

/**
 * 低优先级消费模式，消息出错直接丢弃，适合容忍消息丢失的非重要消息
 *
 * Created by xiong.j on 2017/3/29.
 */
@Slf4j
class AmqConsumerLowPriorityStrategy extends AbstractConsumerStrategy implements AmqConsumerStrategy {

    @Override
    public void onMessage(final AmqConsumerConfig config, final TextMessage message) {
        tryConsume(config, message);
    }

    @SuppressWarnings("unchecked")
    private void tryConsume(final AmqConsumerConfig config, final TextMessage textMessage) {
        try {
            // 反序列化消息
            MessageRecord message = deserializerMessage(config, textMessage.getText());
            // 生成消息ID
            message.setMessageId(textMessage.getJMSMessageID());
            // 重复消费检测
            if (!checkRedeliver(config, message)) {
                // 消费消息
                onMessage(config, message);
            } else {
                log.info("Check redeliver is true, discard this message:" + message);
            }
        } catch (Throwable e) {
            log.warn("Receive message failed. message:" + textMessage, e);
        }
    }
}
