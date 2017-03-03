package com.arto.kafka.consumer;

import com.arto.kafka.config.KafkaConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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

//    @Value("${kafka.servers:172.18.2.192:9092}")
//    private String servers;
//
//    @Value("${kafka.group.id:pergesa-msg}")
//    private String groupId;
//
//    @Value("${kafka.enable.auto.commit:false}")
//    private boolean autoCommit;
//
//    @Value("${kafka.auto.commit.interval.ms:1000}")
//    private int autoCommitIntervalMs;
//
//    @Value("${kafka.fetch.message.max.bytes:1048576}")
//    private int fetchMessageMaxBytes;
//
//    @Value("${kafka.session.timeout.ms:30000}")
//    private int sessionTimeoutMs;
//
//    @Value("${kafka.key.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
//    private String keyDeserializer;
//
//    @Value("${kafka.value.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
//    private String valueDeserializer;

    private ConcurrentMap<String, KafkaConsumer<String, String>> consumerMap
            = new ConcurrentHashMap<String, KafkaConsumer<String, String>>(3);

    private final String defaultKey = "default";

    /**
     * 获取消费者
     *
     * @return
     * @throws Exception
     */
    public KafkaConsumer<String, String> getConsumer() throws Exception {
        if (consumerMap.containsKey(defaultKey)) {
            return consumerMap.get(defaultKey);
        } else {
            return createConsumer();
        }
    }

    private synchronized KafkaConsumer<String, String> createConsumer() throws Exception {
        if (consumerMap.containsKey(defaultKey)) {
            return consumerMap.get(defaultKey);
        }
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
                , KafkaConfigManager.getString(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.2.192:9092"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG
                , KafkaConfigManager.getString(ConsumerConfig.GROUP_ID_CONFIG, "pergesa-msg"));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG
                , KafkaConfigManager.getInt(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1048576));
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG
                , KafkaConfigManager.getInt(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
                , KafkaConfigManager.getString(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
                , "org.apache.kafka.common.serialization.StringDeserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
                , KafkaConfigManager.getString(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
                , "org.apache.kafka.common.serialization.StringDeserializer"));

        prepareEnvironments(props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumerMap.put(defaultKey, consumer);
        log.info("Create kafka consumer successful. config:" + props);
        return consumer;
    }

    @PreDestroy
    public void destroy() {
        for(Map.Entry<String, KafkaConsumer<String, String>> entry : consumerMap.entrySet()){
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
