package com.arto.kafka.consumer;

import com.arto.core.common.MessagePriorityEnum;
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

    public KafkaConsumerThread(final KafkaConsumerWrapper<String, String> consumerWrapper, final KafkaConsumerConfig config
            , final List<ConsumerRecord<String, String>> records) {
        this.consumerWrapper = consumerWrapper;
        this.config = config;
        this.records = records;
    }

    @Override
    public Object call() throws Exception {
        TopicPartition topicPartition = null;

        try {
            for (ConsumerRecord<String, String> record : records) {
                log.debug("Kafka consume message:" + record);
                if (topicPartition == null) {
                    topicPartition = new TopicPartition(record.topic(), record.partition());
                }
                // 处理消息
                KafkaConsumerStrategyFactory.getInstance().getStrategy(config.getPriority()).onMessage(config, record);
                // 提交消费标识 TODO 消息标识批量提交
                if (config.getPriority() != MessagePriorityEnum.LOW.getCode()) {
                    commitSync(topicPartition, new OffsetAndMetadata(record.offset() + 1));
                } else {
                    // 大量异步提交时报coordinator unavailable？？ 所以低优先级走自动提交..
                    // commitAsync(topicPartition, new OffsetAndMetadata(record.offset() + 1));
                }
            }

            consumerWrapper.resume(Collections.singleton(topicPartition));
            log.info("Kafka consumer resume:" + topicPartition + ", thread:" + Thread.currentThread().getName());
        } catch (Throwable t) {
            log.warn("Kafka message consume failed", t);
            throw new MqClientException(t);
        }

        return null;
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
