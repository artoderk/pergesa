package com.arto.kafka.consumer.strategy;

import com.arto.core.common.MessageRecord;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import static com.arto.kafka.common.KUtil.buildMessageId;

/**
 * 低优先级消费模式，适合容忍消息丢失的非重要消息
 * (单条消息处理完成后消费标识异步提交)
 *
 * Created by xiong.j on 2017/1/20.
 */
@Slf4j
class KafkaConsumerLowPriorityStrategy extends AbstractKafkaConsumerStrategy implements KafkaConsumerStrategy {

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
            log.warn("Receive message failed. record=" + record, e);
        }
    }

}
