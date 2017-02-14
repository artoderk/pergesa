package com.arto.kafka.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.MqListener;
import com.arto.event.util.ThreadUtil;
import com.arto.event.util.TypeReferenceUtil;
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
class KafkaConsumerLowPriorityStrategy implements KafkaConsumerStrategy {

    @Override
    public void onMessage(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        tryConsume(config, record);
    }

    @SuppressWarnings("unchecked")
    private void tryConsume(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        // 如果出错，重试消费3次，超过三次丢弃
        for (int i = 1; i <= 3; i++) {
            MessageRecord message = null;
            try {
                // 生成消息ID
                message = buildMessage(config.getListener(), record.value());
                message.setMessageId(buildMessageId(record.partition(), record.offset()));
                // 消费消息
                config.getListener().onMessage(message);
                break;
            } catch (Throwable e) {
                log.warn("Receive message failed. record=" + record, e);
                // 消息处理错误，暂停处理一小会
                ThreadUtil.sleep(5000, Thread.currentThread(), log);
            }
        }
    }

    private MessageRecord buildMessage(MqListener listener, String payload) throws Throwable {
        // 反序列化消息
        return JSON.parseObject(payload, TypeReferenceUtil.getType(listener));
    }
}
