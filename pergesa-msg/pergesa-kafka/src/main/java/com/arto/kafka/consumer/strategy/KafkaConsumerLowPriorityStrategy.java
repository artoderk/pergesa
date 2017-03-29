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
package com.arto.kafka.consumer.strategy;

import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.strategy.AbstractConsumerStrategy;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import static com.arto.kafka.common.KUtil.buildMessageId;

/**
 * 低优先级消费模式，消息出错直接丢弃，适合容忍消息丢失的非重要消息
 *
 * Created by xiong.j on 2017/1/20.
 */
@Slf4j
class KafkaConsumerLowPriorityStrategy extends AbstractConsumerStrategy implements KafkaConsumerStrategy {

    @Override
    public void onMessage(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        tryConsume(config, record);
    }

    @SuppressWarnings("unchecked")
    private void tryConsume(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        try {
            // 反序列化消息
            MessageRecord message = deserializerMessage(config, record.value());
            // 生成消息ID
            message.setMessageId(buildMessageId(record.partition(), record.offset()));
            // 重复消费检测
            if (!checkRedeliver(config, message)) {
                // 消费消息
                onMessage(config, message);
            } else {
                log.info("Discard redelivered message:" + message);
            }
        } catch (Throwable e) {
            log.warn("Receive message failed. record:" + record, e);
        }
    }

}
