/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.arto.amq.bootstrap;

import com.arto.amq.consumer.binding.AmqConsumerBinding;
import com.arto.amq.consumer.binding.AmqConsumerConfig;
import com.arto.amq.producer.binding.AmqProducerBinding;
import com.arto.amq.producer.binding.AmqProducerConfig;
import com.arto.core.bootstrap.MqFactory;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.consumer.ConsumerConfig;
import com.arto.core.consumer.MqConsumer;
import com.arto.core.producer.MqProducer;
import com.arto.core.producer.ProducerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Amq客户端生产者和消息者工厂类
 *
 * Created by xiong.j on 2017/3/21.
 */
@Slf4j
public class AmqClientFactory implements MqFactory {

    private static final ConcurrentMap<String, AmqProducerBinding> producerMap = new ConcurrentHashMap<String, AmqProducerBinding>();

    private static final ConcurrentMap<String, AmqConsumerBinding> consumerMap = new ConcurrentHashMap<String, AmqConsumerBinding>();

    private static final Object lockProducer = new Object();

    private static final Object lockConsumer = new Object();

    /**
     * 根据生产者配置文件生成一个新的生产者
     *
     * @param config
     * @return
     */
    @Override
    public MqProducer buildProducer(ProducerConfig config) {
        if (producerMap.containsKey(config.getDestination())) {
            return producerMap.get(config.getDestination());
        } else {
            synchronized (lockProducer) {
                if (producerMap.containsKey(config.getDestination())) {
                    return producerMap.get(config.getDestination());
                } else {
                    // 生成一个新的生产者
                    AmqProducerBinding producer = new AmqProducerBinding((AmqProducerConfig)config);
                    producerMap.put(config.getDestination(), producer);
                    log.info("Binding Amq com.arto.amq.producer on config : " + config);
                    return producer;
                }
            }
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
        if (consumerMap.containsKey(config.getDestination())) {
            return consumerMap.get(config.getDestination());
        } else {
            synchronized (lockConsumer) {
                if (consumerMap.containsKey(config.getDestination())) {
                    return consumerMap.get(config.getDestination());
                } else {
                    // 生成一个新的消费者
                    AmqConsumerBinding consumer = new AmqConsumerBinding((AmqConsumerConfig)config);
                    consumerMap.put(config.getDestination(), consumer);
                    log.info("Binding Amq com.arto.amq.consumer on config : " + config);
                    return consumer;
                }
            }
        }
    }

    @Override
    public String getMqType() {
        return MqTypeEnum.ACTIVEMQ.getMemo();
    }

    /**
     * 销毁所有的生产者和消息者
     */
    @Override
    public void destroy() {
        for(Map.Entry<String, AmqProducerBinding> entry : producerMap.entrySet()){
            entry.getValue().close();
        }
        producerMap.clear();

        for(Map.Entry<String, AmqConsumerBinding> entry : consumerMap.entrySet()){
            entry.getValue().close();
        }
        consumerMap.clear();
    }

    /**
     * 获取生产者的配置
     *
     * @param destination
     * @return
     */
    public AmqProducerConfig getProducerConfig(String destination){
        if (producerMap.containsKey(destination)) {
            return producerMap.get(destination).getConfig();
        }
        return null;
    }

    /**
     * 获取消费者的配置
     *
     * @param destination
     * @return
     */
    public AmqConsumerConfig getConsumerConfig(String destination){
        if (consumerMap.containsKey(destination)) {
            return consumerMap.get(destination).getConfig();
        }
        return null;
    }
}
