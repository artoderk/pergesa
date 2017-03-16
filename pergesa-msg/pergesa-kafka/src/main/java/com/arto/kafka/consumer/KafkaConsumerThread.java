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
package com.arto.kafka.consumer;

import com.arto.core.exception.MqClientException;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.consumer.strategy.KafkaConsumerStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Kafka消息处理线程
 *
 * Created by xiong.j on 2017/1/18.
 */
@Slf4j
public class KafkaConsumerThread implements Callable{

    /** Kafka消费者包装类 */
    private KafkaConsumerWrapper<String, String> consumerWrapper;

    /** Topic消费者配置 */
    private KafkaConsumerConfig config;

    /** Topic拉取的消息(单个分区) */
    private List<ConsumerRecord<String, String>> records;

    /** 当前已消费数据 */
    private ConsumerRecord<String, String> currentConsumedRecord;

    public KafkaConsumerThread(final KafkaConsumerWrapper<String, String> consumerWrapper, final KafkaConsumerConfig config
            , final List<ConsumerRecord<String, String>> records) {
        this.consumerWrapper = consumerWrapper;
        this.config = config;
        this.records = records;
    }

    @Override
    public Boolean call() throws Exception {
        TopicPartition topicPartition = null;
        ConsumerRecord<String, String> record;
        try {
            for (int i = 0; i < records.size(); i++) {
                record = records.get(i);
                if (topicPartition == null) {
                    topicPartition = new TopicPartition(record.topic(), record.partition());
                }
                // 处理消息
                KafkaConsumerStrategyFactory.getInstance().getStrategy(config.getPriority()).onMessage(config, record);
                log.debug("Kafka consume message:" + record);

                // 提交消费标识
                currentConsumedRecord = record;
                //if (config.getPriority() != MessagePriorityEnum.LOW.getCode()) {
                if (i % config.getAckSize() == 0 || i == records.size() - 1) {
                    commitSync(topicPartition, new OffsetAndMetadata(currentConsumedRecord.offset() + 1));
                }
                //} else {
                    // 大量异步提交时报coordinator unavailable？？ 所以低优先级走自动提交..
                    // commitAsync(topicPartition, new OffsetAndMetadata(currentConsumedRecord.offset() + 1));
                //}
            }
        } catch (Throwable t) {
            log.warn("Kafka message consume failed", t);
            throw new MqClientException(t);
        } finally {
            if (topicPartition != null) {
                resume(topicPartition);
            }
        }

        return true;
    }

    /**
     * 立即提交消费标识
     */
    public void commitOffsetImmediately(){
        if (currentConsumedRecord == null) return;

        commitSync(new TopicPartition(currentConsumedRecord.topic()
                , currentConsumedRecord.partition())
                , new OffsetAndMetadata(currentConsumedRecord.offset() + 1));
    }

    private void resume(TopicPartition topicPartition){
        currentConsumedRecord = null;
        consumerWrapper.resume(Collections.singleton(topicPartition));
        log.info("Kafka consumer resume:" + topicPartition + ", thread:" + Thread.currentThread().getName());
    }

    private void commitSync(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(topicPartition, offsetAndMetadata);
        consumerWrapper.commitSync(offsets);
    }

    private void commitAsync(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(topicPartition, offsetAndMetadata);
        consumerWrapper.commitAsync(offsets, null);
    }
}
