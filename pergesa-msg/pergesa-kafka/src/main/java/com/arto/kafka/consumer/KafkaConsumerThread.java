package com.arto.kafka.consumer;

import com.arto.core.exception.MqClientException;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.consumer.strategy.KafkaConsumerStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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

    /** Kafka消费者 */
    private KafkaConsumer<String, String> consumer;

    /** Topic消费者配置 */
    private KafkaConsumerConfig config;

    /** Topic拉取的消息(单个分区) */
    private List<ConsumerRecord<String, String>> records;

    public KafkaConsumerThread(final KafkaConsumer<String, String> consumer, final KafkaConsumerConfig config
            , final List<ConsumerRecord<String, String>> records) {
        this.consumer = consumer;
        this.config = config;
        this.records = records;
    }

    @Override
    public Object call() throws Exception {
        TopicPartition topicPartition = null;

        try {
            for (ConsumerRecord<String, String> record : records) {
                log.debug("Consume message:" + record);
                if (topicPartition == null) {
                    topicPartition = new TopicPartition(record.topic(), record.partition());
                }
                // 处理消息
                KafkaConsumerStrategyFactory.getInstance().getStrategy(config.getPriority()).onMessage(config, record);
                // 提交消费标识 TODO 根据优先级处理消息标识与重试
                commitSync(topicPartition, new OffsetAndMetadata(record.offset() + 1));
            }

            // TODO 使用单独线程管理消费的暂停与恢复
            consumer.resume(Collections.singleton(topicPartition));
            log.info("Consumer resume:" + topicPartition);
        } catch (Throwable t) {
            log.warn("Consume failed", t);
            throw new MqClientException(t);
        }

        return null;
    }

    private void commitSync(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(topicPartition, offsetAndMetadata);
        consumer.commitSync(offsets);
    }

    private void commitAsync(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(topicPartition, offsetAndMetadata);
        consumer.commitAsync(offsets, null);
    }
}
