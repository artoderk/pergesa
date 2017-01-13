package com.arto.kafka.build;

import com.arto.core.build.MqClient;
import com.arto.core.build.MqFactory;
import com.arto.core.comsumer.ConsumerConfig;
import com.arto.core.comsumer.MqConsumer;
import com.arto.core.exception.MqClientException;
import com.arto.core.producer.MqProducer;
import com.arto.core.producer.ProducerConfig;
import com.arto.kafka.common.Constants;
import com.arto.kafka.producer.binding.KafkaProducerBinding;
import com.arto.kafka.producer.binding.KafkaProducerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Kafka客户端生产者和消息者工厂类
 *
 * Created by xiong.j on 2017/1/12.
 */
@Slf4j
public class KafkaClientFactory implements MqFactory {

    private static final KafkaClientFactory instance = new KafkaClientFactory();

    private static final ConcurrentMap<String, KafkaProducerBinding> producerMap = new ConcurrentHashMap<String, KafkaProducerBinding>();

    private static final ConcurrentMap<String, MqConsumer> consumerMap = new ConcurrentHashMap<String, MqConsumer>();

    private final Object lockProducer = new Object();

    private final Object lockConsumer = new Object();

    static {
        // 注册Kafka客户端
        MqClient.setMqFactory(Constants.KAFKA, instance);
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
                        // TODO 生成一个新的消费者
                        consumerMap.put(config.getDestination(), null);
                        log.info("Binding kafka consumer on config : " + config);
                        return null;
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
        producerMap.clear();
        consumerMap.clear();
    }
}
