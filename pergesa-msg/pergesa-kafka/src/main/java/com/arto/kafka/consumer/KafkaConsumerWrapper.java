package com.arto.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

/**
 * Kafka消费者包装类，确保同一个消费者在多线程环境中是同步调用的.
 *
 * Created by xiong.j on 2017/2/28.
 */
@Slf4j
public class KafkaConsumerWrapper<K, V> {

    private final KafkaConsumer<K, V> consumer;

    public KafkaConsumerWrapper(final KafkaConsumer<K, V> consumer) {
        this.consumer = consumer;
    }

    public synchronized void pause(Collection<TopicPartition> partitions) {
        consumer.pause(partitions);
    }

    public synchronized void resume(Collection<TopicPartition> partitions) {
        consumer.resume(partitions);
    }

    public synchronized Set<TopicPartition> paused() {
        return consumer.paused();
    }

    public synchronized void commitSync() {
        consumer.commitSync();
    }

    public synchronized void commitSync(final Map<TopicPartition, OffsetAndMetadata> offsets) {
        consumer.commitSync(offsets);
    }

    public synchronized void commitAsync() {
        commitAsync(null);
    }

    public synchronized void commitAsync(OffsetCommitCallback callback) {
        consumer.commitAsync(callback);
    }

    public synchronized void commitAsync(final Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
        consumer.commitAsync(offsets, callback);
    }

    public synchronized List<List<ConsumerRecord<K, V>>> sequencePoll(long timeout) {
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

    public synchronized Set<String> subscription() {
        return consumer.subscription();
    }

    public synchronized void subscribe(Collection<String> topics) {
        consumer.subscribe(topics);
    }

    public synchronized void subscribe(Collection<String> topics, ConsumerRebalanceListener listener) {
        consumer.subscribe(topics, listener);
    }
}
