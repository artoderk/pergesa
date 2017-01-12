package com.arto.kafka.producer;

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

    @Value("${kafka.servers}")
    private String servers = "172.18.2.192:9092";

    @Value("${kafka.client.id}")
    private String client = "pergesa-msg";

    @Value("${kafka.retries}")
    private int retries = 3;

    @Value("${kafka.batch.size}")
    private int batchSize = 16384;

    @Value("${kafka.linger.ms}")
    private int lingerMs = 1;

    @Value("${kafka.buffer.memory}")
    private int bufferMemory = 33554432;

    @Value("${kafka.key.serializer}")
    private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";

    @Value("${kafka.value.serializer}")
    private String valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

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

    private synchronized KafkaProducer<String, String> createProducer(final Integer priority) throws Exception {
        if (producerMap.containsKey(priority)) {
            return producerMap.get(priority);
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
        props.put("client.id", client);
        props.put("acks", String.valueOf(priority));
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

    @PreDestroy
    public synchronized void destroy() throws Exception {
        for(Map.Entry<Integer, KafkaProducer<String, String>> entry : producerMap.entrySet()){
            entry.getValue().close();
        }
        log.info("Destroy kafka producer successful. : ");
    }

    /**
     * 根据优先级准备不同的配置
     *
     * @param priority 优先级
     * @param props 配置
     */
    protected void prepareEnvironments(final Integer priority, final Properties props) {
    }
}
