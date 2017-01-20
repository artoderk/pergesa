package com.arto.kafka.consumer;

import com.arto.core.common.MessageRecord;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.consumer.strategy.KConsumerStrategyFactory;
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
    private KafkaConsumer<String, MessageRecord> consumer;

    /** Topic消费者配置 */
    private KafkaConsumerConfig config;

    /** Topic拉取的消息(单个分区) */
    private List<ConsumerRecord<String, MessageRecord>> records;

    public KafkaConsumerThread(final KafkaConsumer<String, MessageRecord> consumer, final KafkaConsumerConfig config
            , final List<ConsumerRecord<String, MessageRecord>> records) {
        this.consumer = consumer;
        this.config = config;
        this.records = records;
    }

    @Override
    public Object call() throws Exception {
        TopicPartition topicPartition = new TopicPartition(records.get(0).topic(), records.get(0).partition());

        for (ConsumerRecord<String, MessageRecord> record : records) {
            KConsumerStrategyFactory.getInstance().getStrategy(config.getPriority()).onMessage(config, record);

            Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(topicPartition, new OffsetAndMetadata(record.offset() + 1));
            consumer.commitSync();
        }
        // TODO 根据优先级处理消息标识与重试

        // TODO 使用单独线程管理消费的暂停与恢复
        consumer.resume(Collections.singleton(topicPartition));
        return null;
    }

    private void commitOffset(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(topicPartition, offsetAndMetadata);
        consumer.commitSync(offsets);
    }
}
