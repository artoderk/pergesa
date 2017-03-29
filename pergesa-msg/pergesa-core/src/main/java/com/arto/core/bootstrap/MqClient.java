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
package com.arto.core.bootstrap;

import com.arto.core.common.DataPipeline;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.config.MqConfigManager;
import com.arto.core.consumer.ConsumerConfig;
import com.arto.core.consumer.MqConsumer;
import com.arto.core.event.MqEvent;
import com.arto.core.exception.MqClientException;
import com.arto.core.producer.MqProducer;
import com.arto.core.producer.ProducerConfig;
import com.arto.event.common.Destroyable;
import com.arto.event.util.SpringDestroyableUtil;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 消息中间件客户端入口，可灵活配备不同的消息中间件，默认实现了Kafka和ActiveMq。
 *
 * Created by xiong.j on 2017/1/11.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MqClient implements Destroyable{

    private static final MqClient instance = new MqClient();

    private static final ConcurrentMap<String, MqFactory> factoryMap
            = new ConcurrentHashMap<String, MqFactory>(2);

    private static final ConcurrentMap<String, DataPipeline<MqEvent>> pipelineMap
            = new ConcurrentHashMap<String, DataPipeline<MqEvent>>(2);

    static {
        // 初始化
        init();
    }

    /**
     *  使用SPI加载默认的消息中件间客户端实现
     */
    private static void init() {
        ServiceLoader<MqFactory> serviceLoader = ServiceLoader.load(MqFactory.class);
        Iterator<MqFactory> mqFactories = serviceLoader.iterator();
        MqFactory mqFactory;
        while(mqFactories.hasNext()){
            // 加载客户端实现
            mqFactory = mqFactories.next();
            factoryMap.put(mqFactory.getMqType(), mqFactory);

            // 加载消息队列
            DataPipeline<MqEvent> dataPipeline = new DataPipeline<MqEvent>(MqConfigManager.getInt("mq.pipeline.size", 30000));
            pipelineMap.put(mqFactory.getMqType(), dataPipeline);
        }
        // 注册勾子
        SpringDestroyableUtil.add("mqClient", instance);
    }

    /**
     * 根据生产者配置文件生成一个新的生产者
     *
     * @param config
     * @return
     */
    public static MqProducer buildProducer(ProducerConfig config){
        verifyConfig(config);
        if (factoryMap.containsKey(config.getType().getMemo())) {
            return factoryMap.get(config.getType().getMemo()).buildProducer(config);
        } else {
            throw new MqClientException("Not support this MQ type:" + config.getType().getMemo());
        }
    }

    /**
     * 根据消费者配置文件生成一个新的消费者
     *
     * @param config
     * @return
     */
    public static MqConsumer buildConsumer(ConsumerConfig config){
        verifyConfig(config);
        if (factoryMap.containsKey(config.getType().getMemo())) {
            return factoryMap.get(config.getType().getMemo()).buildConsumer(config);
        } else {
            throw new MqClientException("Not support this MQ type:" + config.getType().getMemo());
        }
    }

//    /**
//     * 添加一个新的消息中件客户端生成器
//     *
//     * @param key
//     * @param factory
//     * @return
//     */
//    public synchronized static void addMqFactory(String key, MqFactory factory){
//        if (factoryMap.containsKey(key)) {
//            throw new MqClientException("Can't override exist MqFactory, key:" + key);
//        } else {
//            factoryMap.put(key, factory);
//        }
//    }

    /**
     * 获取消息中件客户端生成器
     */
    public static MqFactory getMqFactory(String mqType){
        if (factoryMap.containsKey(mqType)) {
            return factoryMap.get(mqType);
        }
        return null;
    }

    public static DataPipeline<MqEvent> getPipeline(String mqType){
        if (pipelineMap.containsKey(mqType)) {
            return pipelineMap.get(mqType);
        }
        throw new MqClientException("Not support this MQ type:" + mqType);
    }


    /**
     * 销毁所有的生产者和消息者
     */
    public void destroy() {
        for(Map.Entry<String, MqFactory> entry : factoryMap.entrySet()){
            entry.getValue().destroy();
        }
        for(Map.Entry<String, DataPipeline<MqEvent>> entry : pipelineMap.entrySet()){
            entry.getValue().clear();
        }
        factoryMap.clear(); // TODO 为避免启动时MQ连不上，这里可能不能清除
        pipelineMap.clear();
    }

    private static void verifyConfig(MqConfig config){
        if (Strings.isNullOrEmpty(config.getDestination())) {
            throw new MqClientException("Parameter 'dest' can't be null" + config);
        }

        if (config instanceof ProducerConfig) {
            ProducerConfig producerConfig = (ProducerConfig) config;
            if ((producerConfig.getPriority().getCode() > MessagePriorityEnum.LOW.getCode())
                    || (producerConfig.getPriority().getCode() < MessagePriorityEnum.HIGH.getCode())) {
                throw new MqClientException("Not support this priority! ProducerConfig:" + producerConfig);
            }
            if (producerConfig.getPriority() == MessagePriorityEnum.HIGH && producerConfig.getCallback() != null) {
                throw new MqClientException("Transaction messages can't send by asynchronous! ProducerConfig:" + producerConfig);
            }
        }
    }
}
