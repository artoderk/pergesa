package com.arto.kafka.producer;

import com.arto.kafka.common.KAcksEnum;
import com.arto.kafka.config.KafkaConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
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

    /*@Value("${kafka.servers:172.18.2.192:9092}")
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
    private String valueSerializer;*/

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
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
                , KafkaConfigManager.getString(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.2.192:9092"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG
                , KafkaConfigManager.getString(ProducerConfig.CLIENT_ID_CONFIG, "pergesa-msg"));
        props.put(ProducerConfig.ACKS_CONFIG
                , String.valueOf(convert2Ack(priority)));
        props.put(ProducerConfig.RETRIES_CONFIG
                , KafkaConfigManager.getInt(ProducerConfig.RETRIES_CONFIG, 3));
        props.put(ProducerConfig.BATCH_SIZE_CONFIG
                , KafkaConfigManager.getInt(ProducerConfig.BATCH_SIZE_CONFIG, 16384));
        props.put(ProducerConfig.LINGER_MS_CONFIG
                , KafkaConfigManager.getInt(ProducerConfig.LINGER_MS_CONFIG, 1));
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG
                , KafkaConfigManager.getInt(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
                , KafkaConfigManager.getString(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
                , "org.apache.kafka.common.serialization.StringSerializer"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
                , KafkaConfigManager.getString(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
                , "org.apache.kafka.common.serialization.StringSerializer"));

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
