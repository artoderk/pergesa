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

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

/**
 * Kafka消费者包装类，确保同一消费者在多线程环境中是同步调用的.
 *
 * Created by xiong.j on 2017/2/28.
 */
@Slf4j
public class KafkaConsumerWrapper<K, V> {

    private final KafkaConsumer<K, V> consumer;

    KafkaConsumerWrapper(final KafkaConsumer<K, V> consumer) {
        this.consumer = consumer;
    }

    private synchronized void pause(Collection<TopicPartition> partitions) {
        consumer.pause(partitions);
    }

    synchronized void resume(Collection<TopicPartition> partitions) {
        consumer.resume(partitions);
    }

    synchronized Set<TopicPartition> paused() {
        return consumer.paused();
    }

    synchronized void commitSync() {
        consumer.commitSync();
    }

    synchronized void commitSync(final Map<TopicPartition, OffsetAndMetadata> offsets) {
        consumer.commitSync(offsets);
    }

    synchronized void commitAsync() {
        commitAsync(null);
    }

    synchronized void commitAsync(OffsetCommitCallback callback) {
        consumer.commitAsync(callback);
    }

    synchronized void commitAsync(final Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
        consumer.commitAsync(offsets, callback);
    }

    synchronized List<List<ConsumerRecord<K, V>>> sequencePoll(long timeout) {
        List<List<ConsumerRecord<K, V>>> list = new ArrayList<List<ConsumerRecord<K, V>>>();

        if (consumer.subscription().size() > 0) {
            ConsumerRecords<K, V> records = consumer.poll(timeout);
            if (!records.isEmpty()) {
                Set<TopicPartition> partitions = records.partitions();
                for (TopicPartition partition : partitions) {
                    // 将拉取到的消息按Topic分类
                    list.add(records.records(partition));

                }
                // 已拉取到消息的暂停拉取直接消息消费完成
                pause(partitions);
                log.info("Kafka poll topic:" + consumer.subscription() + ", message size:" + records.count() + ", and pause:" + partitions);
            }
        }
        return list;
    }

    synchronized Set<String> subscription() {
        return consumer.subscription();
    }

    synchronized void subscribe(Collection<String> topics) {
        consumer.subscribe(topics);
    }

    synchronized void subscribe(Collection<String> topics, ConsumerRebalanceListener listener) {
        consumer.subscribe(topics, listener);
    }
}
