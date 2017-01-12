package com.arto.kafka.producer;

import com.arto.core.exception.MqClientException;
import com.arto.kafka.event.KafkaEvent;
import com.google.common.base.Strings;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiong.j on 2016/7/21.
 */
@Slf4j
@Component
public class KafkaMessageProducer {

    @Value("${kafka.producer.timeout}")
    private int timeout = 30;

    @Autowired
    private KafkaMessageProducerFactory factory;

    /**
     * 发送消息
     *
     * @param event
     * @throws MqClientException
     */
    public void send(KafkaEvent event) throws MqClientException {
        try {
            log.info("Kafka process event:", event);
            Future<RecordMetadata> future;
            if (event.getPartition() == -1) {
                // 没有设置分区
                if (Strings.isNullOrEmpty(event.getKey())) {
                    // 没有设置Hash主键
                    future = factory.getProducer(event.getPriority()).send(
                            new ProducerRecord<String, String>(event.getDestination(), event.getPayload()));
                } else {
                    future = factory.getProducer(event.getPriority()).send(
                            new ProducerRecord<String, String>(event.getDestination(), event.getKey(), event.getPayload()));
                }
            } else {
                future = factory.getProducer(event.getPriority()).send(
                        new ProducerRecord<String, String>(event.getDestination(), event.getPartition(), event.getKey(), event.getPayload()));
            }
            RecordMetadata metadata = future.get(timeout, TimeUnit.SECONDS);
            log.info("Send payload: {topic:" + metadata.topic() + ", partition:" + metadata.partition() + ", offset:" + metadata.offset() + "}");
        } catch (Throwable e) {
            log.error("Send payload error: ", e);
            throw new MqClientException("Send payload error: " + event, e);
        }
    }
}
