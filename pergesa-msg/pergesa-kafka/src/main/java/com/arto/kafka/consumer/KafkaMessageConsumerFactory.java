package com.arto.kafka.consumer;

import com.arto.core.common.MessageRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiong.j on 2016/7/21.
 */
@Slf4j
@Component
public class KafkaMessageConsumerFactory {

    @Value("${kafka.servers:172.18.2.192:9092}")
    private String servers;

    @Value("${kafka.group.id:pergesa-msg}")
    private String groupId;

    @Value("${kafka.enable.auto.commit:false}")
    private boolean autoCommit;

    @Value("${kafka.auto.commit.interval.ms:1000}")
    private int autoCommitIntervalMs;

    @Value("${kafka.fetch.message.max.bytes:1048576}")
    private int fetchMessageMaxBytes;

    @Value("${kafka.session.timeout.ms:30000}")
    private int sessionTimeoutMs;

    @Value("${kafka.key.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private String keyDeserializer;

    @Value("${kafka.value.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private String valueDeserializer;

    private ConcurrentMap<String, KafkaConsumer<String, MessageRecord>> consumerMap
            = new ConcurrentHashMap<String, KafkaConsumer<String, MessageRecord>>(3);

    private final String defaultKey = "default";

    /**
     * 获取消费者
     *
     * @return
     * @throws Exception
     */
    public KafkaConsumer<String, MessageRecord> getConsumer() throws Exception {
        if (consumerMap.containsKey(defaultKey)) {
            return consumerMap.get(defaultKey);
        } else {
            return createConsumer();
        }
    }

    private synchronized KafkaConsumer<String, MessageRecord> createConsumer() throws Exception {
        if (consumerMap.containsKey(defaultKey)) {
            return consumerMap.get(defaultKey);
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
        props.put("group.id", groupId);
        props.put("enable.auto.commit", String.valueOf(autoCommit));
        props.put("auto.commit.interval.ms", autoCommitIntervalMs);
        props.put("fetch.message.max.bytes", fetchMessageMaxBytes);
        props.put("session.timeout.ms", sessionTimeoutMs);
        props.put("key.deserializer", keyDeserializer);
        props.put("value.deserializer", valueDeserializer);

        prepareEnvironments(props);
        KafkaConsumer<String, MessageRecord> consumer = new KafkaConsumer<String, MessageRecord>(props);
        consumerMap.put(defaultKey, consumer);
        log.info("Create kafka consumer successful. config:" + props);
        return consumer;
    }

    @PreDestroy
    public synchronized void destroy() throws Exception {
        for(Map.Entry<String, KafkaConsumer<String, MessageRecord>> entry : consumerMap.entrySet()){
            entry.getValue().close();
        }
        log.info("Destroy kafka consumer successful.");
    }

    /**
     * 根据优先级准备不同的配置
     *
     * @param props 配置
     */
    protected void prepareEnvironments(final Properties props) {
    }
}
