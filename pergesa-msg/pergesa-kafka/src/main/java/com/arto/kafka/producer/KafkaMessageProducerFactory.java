package com.arto.kafka.producer;

import com.arto.kafka.common.KAcksEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiong.j on 2016/7/21.
 */
@Slf4j
@Component
public class KafkaMessageProducerFactory {

    @Value("${kafka.servers:172.18.2.192:9092}")
    private String servers;

    @Value("${kafka.client.id:pergesa-msg}")
    private String client;

    @Value("${kafka.retries:3}")
    private int retries;

    @Value("${kafka.batch.size:16384}")
    private int batchSize;

    @Value("${kafka.linger.ms:1}")
    private int lingerMs;

    @Value("${kafka.buffer.memory:33554432}")
    private int bufferMemory;

    @Value("${kafka.key.serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String keySerializer;

    @Value("${kafka.value.serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String valueSerializer;

    private Map<Integer, KafkaProducer<String, String>> producerMap = new ConcurrentHashMap<Integer, KafkaProducer<String, String>>(3);

    /**
     * 根据优先级获取生产者
     *
     * @param priority
     * @return
     * @throws Exception
     */
    public KafkaProducer<String, String> getProducer(final Integer priority) throws Exception {
        if (producerMap.containsKey(priority)) {
            return producerMap.get(priority);
        } else {
            return createProducer(priority);
        }
    }

    @PreDestroy
    public synchronized void destroy() throws Exception {
        for(Map.Entry<Integer, KafkaProducer<String, String>> entry : producerMap.entrySet()){
            entry.getValue().close();
        }
        log.info("Destroy kafka producer successful.");
    }

    /**
     * 根据优先级准备不同的配置
     *
     * @param priority 优先级
     * @param props 配置
     */
    protected void prepareEnvironments(final Integer priority, final Properties props) {
    }

    private synchronized KafkaProducer<String, String> createProducer(final Integer priority) throws Exception {
        if (producerMap.containsKey(priority)) {
            return producerMap.get(priority);
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
        props.put("client.id", client);
        props.put("acks", String.valueOf(convert2Ack(priority)));
        props.put("retries", retries);
        props.put("batch.size", batchSize);
        props.put("linger.ms", lingerMs);
        props.put("buffer.memory", bufferMemory);
        props.put("key.serializer", keySerializer);
        props.put("value.serializer", valueSerializer);

        prepareEnvironments(priority, props);
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
        producerMap.put(priority, producer);
        log.info("Create kafka producer successful with acks: " + priority);
        return producer;
    }

    private int convert2Ack(final Integer priority){
        // 优先级转换为Kafka的acks
        if (priority == -1 || priority == 1){
            return KAcksEnum.ACK_ALL.getCode();
        } else if (priority == 2){
            return KAcksEnum.ACK_LEADER.getCode();
        } else {
            return KAcksEnum.ACK_NOWAIT.getCode();
        }
    }
}
