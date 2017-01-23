package com.arto.kafka.producer;

import com.arto.core.exception.MqClientException;
import com.arto.kafka.event.KafkaEvent;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * TODO info改debug
 *
 * Created by xiong.j on 2016/7/21.
 */
@Getter
@Setter
@Slf4j
@Component
public class KafkaMessageProducer {

    /** 同步发送时的超时时间 */
    @Value("${kafka.producer.timeout:30}")
    private int timeout;

    /** 生产者工厂 */
    @Autowired
    private KafkaMessageProducerFactory factory;

    /**
     * 发送消息
     *
     * @param event
     * @throws MqClientException
     */
    public void send(final KafkaEvent event) throws MqClientException {
        try {
            log.info("Kafka process event:", event);
            Future future = null;
            ProducerRecord producerRecord = null;
            if (event.getPartition() == -1) {
                // 没有设置分区
                if (Strings.isNullOrEmpty(event.getKey())) {
                    // 没有设置Hash主键
                    producerRecord = new ProducerRecord<String, String>(event.getDestination(), event.getPayload());
                } else {
                    producerRecord = new ProducerRecord<String, String>(event.getDestination(), event.getKey(), event.getPayload());
                }
            } else {
                producerRecord = new ProducerRecord<String, String>(event.getDestination(), event.getPartition(), event.getKey(), event.getPayload());
            }

            if (event.getPriority() != 3) {
                // 同步发送
                future = factory.getProducer(event.getPriority()).send(producerRecord);
                RecordMetadata metadata = (RecordMetadata) future.get(timeout, TimeUnit.SECONDS);
                log.info("Kafka Send to topic:" + event.getDestination() + ", partition" + metadata.partition() + ", message:" + event.getPayload());
            } else {
                // 异步发送
                if (event.getCallback() != null) {
                    // 有同调
                    future = factory.getProducer(event.getPriority()).send(producerRecord, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            event.getCallback().onCompletion(event);
                        }
                    });
                } else {
                    // 无回调
                    future = factory.getProducer(event.getPriority()).send(producerRecord, null);
                }
                log.info("Kafka Asynchronously send to topic:" + event.getDestination() + ", message:" + event.getPayload());
            }
        } catch (Throwable e) {
            throw new MqClientException("Kafka send message failed: " + event, e);
        }
    }
}
