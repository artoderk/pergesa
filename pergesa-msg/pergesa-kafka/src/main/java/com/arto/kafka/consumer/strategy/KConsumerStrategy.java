package com.arto.kafka.consumer.strategy;

import com.arto.core.common.MessageRecord;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Kafka消费策略接口
 *
 * Created by xiong.j on 2017/1/20.
 */
public interface KConsumerStrategy {

    /**
     * 消费消息
     *
     * @param config
     * @param record
     */
    void onMessage(final KafkaConsumerConfig config, final ConsumerRecord<String, MessageRecord> record);

}
