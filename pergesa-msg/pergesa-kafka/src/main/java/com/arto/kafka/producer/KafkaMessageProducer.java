package com.arto.kafka.producer;

import com.alibaba.fastjson.JSON;
import com.arto.core.exception.MqClientException;
import com.arto.event.util.StringUtil;
import com.arto.kafka.common.KMessageRecord;
import com.arto.kafka.common.KUtil;
import com.arto.kafka.config.KafkaConfigManager;
import com.arto.kafka.event.KafkaProduceEvent;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Kafka消息生产者
 *
 * Created by xiong.j on 2016/7/21.
 */
@Getter
@Setter
@Slf4j
@Component
public class KafkaMessageProducer {

    /** 同步发送时的超时时间 */
    private int timeout = KafkaConfigManager.getInt("kafka.producer.timeout", 10);

    /** 生产者工厂 */
    @Autowired
    private KafkaMessageProducerFactory factory;

    /**
     * 发送消息
     *
     * @param event
     * @throws MqClientException
     */
    public void send(final KafkaProduceEvent event) throws MqClientException {
        Future future = null;
        ProducerRecord<String, String> producerRecord = null;

        try {
            // 序列化消息 并检测是否超过1M
            String payload = StringUtil.checkSize(event.getPayload(), 1048576);
            if (event.getPartition() == -1) {
                // 没有设置分区
                if (Strings.isNullOrEmpty(event.getKey())) {
                    // 没有设置Hash主键
                    producerRecord = new ProducerRecord<String, String>(event.getDestination(), payload);
                } else {
                    producerRecord = new ProducerRecord<String, String>(event.getDestination(), event.getKey(), payload);
                }
            } else {
                producerRecord = new ProducerRecord<String, String>(event.getDestination(), event.getPartition(), event.getKey(), payload);
            }

            if (event.getPriority() != 3 && event.getCallback() == null) {
                // 同步发送
                synSend(event, producerRecord);
            } else {
                // 异步发送
                asynSend(event, producerRecord);
            }
        } catch (Throwable e) {
            throw new MqClientException("Kafka send message failed: " + event, e);
        }
    }

    private RecordMetadata synSend(final KafkaProduceEvent event, final ProducerRecord<String, String> producerRecord) throws Throwable {
        // 同步发送
        Future future = factory.getProducer(event.getPriority()).send(producerRecord);
        RecordMetadata metadata = (RecordMetadata) future.get(timeout, TimeUnit.SECONDS);
        log.debug("Kafka send to topic:" + event.getDestination() + ", partition" + metadata.partition() + ", message:" + event.getPayload());
        return metadata;
    }

    @SuppressWarnings("unchecked")
    private Future asynSend(final KafkaProduceEvent event, final ProducerRecord<String, String> producerRecord) throws Throwable {
        Future future;
        // 异步发送
        if (event.getCallback() != null) {
            // 有回调
            future = factory.getProducer(event.getPriority()).send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    // 设置MessageId
                    KMessageRecord kMessageRecord = (KMessageRecord)event.getPayload();
                    kMessageRecord.setMessageId(KUtil.buildMessageId(metadata.partition(), metadata.offset()));
                    event.getCallback().onCompletion(event);
                }
            });
        } else {
            // 无回调
            future = factory.getProducer(event.getPriority()).send(producerRecord, null);
        }
        log.debug("Kafka Asynchronously send to topic:" + event.getDestination() + ", message:" + event.getPayload());
        return future;
    }
}
