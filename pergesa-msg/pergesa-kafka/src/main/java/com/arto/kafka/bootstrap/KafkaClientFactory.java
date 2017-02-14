package com.arto.kafka.bootstrap;

import com.arto.core.bootstrap.MqClient;
import com.arto.core.bootstrap.MqFactory;
import com.arto.core.consumer.ConsumerConfig;
import com.arto.core.consumer.MqConsumer;
import com.arto.core.exception.MqClientException;
import com.arto.core.producer.MqProducer;
import com.arto.core.producer.ProducerConfig;
import com.arto.kafka.common.Constants;
import com.arto.kafka.consumer.binding.KafkaConsumerBinding;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.producer.binding.KafkaProducerBinding;
import com.arto.kafka.producer.binding.KafkaProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Kafka客户端生产者和消息者工厂类
 *
 * Created by xiong.j on 2017/1/12.
 */
@Slf4j
@Component
public class KafkaClientFactory implements MqFactory {

    private static final KafkaClientFactory instance = new KafkaClientFactory();

    private static final ConcurrentMap<String, KafkaProducerBinding> producerMap = new ConcurrentHashMap<String, KafkaProducerBinding>();

    private static final ConcurrentMap<String, KafkaConsumerBinding> consumerMap = new ConcurrentHashMap<String, KafkaConsumerBinding>();

    private static final Object lockProducer = new Object();

    private static final Object lockConsumer = new Object();

    static {
        // 注册Kafka客户端
        MqClient.setMqFactory(Constants.KAFKA, instance);
    }

    /**
     * Kafka客户端实例
     *
     * @return
     */
    public KafkaClientFactory getInstance() {
        return instance;
    }
    /**
     * 根据生产者配置文件生成一个新的生产者
     *
     * @param config
     * @return
     */
    @Override
    public MqProducer buildProducer(ProducerConfig config) {
        if (config instanceof KafkaProducerConfig) {
            if (producerMap.containsKey(config.getDestination())) {
                return producerMap.get(config.getDestination());
            } else {
                synchronized (lockProducer) {
                    if (producerMap.containsKey(config.getDestination())) {
                        return producerMap.get(config.getDestination());
                    } else {
                        // 生成一个新的生产者
                        KafkaProducerBinding producer = new KafkaProducerBinding((KafkaProducerConfig)config);
                        producerMap.put(config.getDestination(), producer);
                        log.info("Binding kafka producer on config : " + config);
                        return producer;
                    }
                }
            }
        } else {
            throw new MqClientException("Not support this config:" + config);
        }
    }

    /**
     * 根据消费者配置文件生成一个新的消费者
     *
     * @param config
     * @return
     */
    @Override
    public MqConsumer buildConsumer(ConsumerConfig config) {
        if (config instanceof KafkaConsumerConfig) {
            if (consumerMap.containsKey(config.getDestination())) {
                return consumerMap.get(config.getDestination());
            } else {
                synchronized (lockConsumer) {
                    if (consumerMap.containsKey(config.getDestination())) {
                        return consumerMap.get(config.getDestination());
                    } else {
                        // 生成一个新的消费者
                        KafkaConsumerBinding consumer = new KafkaConsumerBinding((KafkaConsumerConfig)config);
                        consumerMap.put(config.getDestination(), consumer);
                        log.info("Binding kafka consumer on config : " + config);
                        return consumer;
                    }
                }
            }
        } else {
            throw new MqClientException("Not support this config:" + config);
        }
    }

    /**
     * 销毁所有的生产者和消息者
     */
    @Override
    public void destroy() {
        for(Map.Entry<String, KafkaProducerBinding> entry : producerMap.entrySet()){
            entry.getValue().close();
            break;
        }
        producerMap.clear();
        consumerMap.clear();
    }
}
