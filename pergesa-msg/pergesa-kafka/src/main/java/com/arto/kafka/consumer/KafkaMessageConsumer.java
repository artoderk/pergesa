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
package com.arto.kafka.consumer;

import com.arto.event.util.ThreadUtil;
import com.arto.kafka.consumer.binding.KafkaConsumerBinding;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO 目前为一个消费者对应多个TOPIC，如果TOPIC太多会造成严重的线程竞争，可能需改为一个TOPIC对应一消费者
 *
 * Created by xiong.j on 2017/1/16.
 */
@Slf4j
@Component
public class KafkaMessageConsumer {

    /** 消费者者工厂 */
    @Autowired
    private KafkaMessageConsumerFactory factory;

    /** 消息拉取线程 */
    private volatile KafkaMessagePollThread pollThread;

    /** 消息拉取线程关闭Flag */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    /**
     * 初始化
     */
    public void init(){
        try {
            pollThread = new KafkaMessagePollThread(factory.getConsumer());
            new Thread(pollThread, "KafkaMessagePollThread").start();
        } catch (Throwable e) {
            log.error("kafka consumer init failed.");
        }
    }

    /**
     * 订阅Topic
     *
     * @param kafkaConsumerBinding
     */
    public void subscribe(final KafkaConsumerBinding kafkaConsumerBinding, ConsumerRebalanceListener listener) {
        if (pollThread == null) {
            synchronized (this){
                if (pollThread == null) {
                    init();
                }
            }
        }
        pollThread.subscribe(kafkaConsumerBinding, listener);
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        log.info("Kafka Consumer poll thread is destroyed.");
        closeFlag.set(true);
    }

    /**
     * Kafka消息拉取线程，一个线程对应多个Topic
     */
    private class KafkaMessagePollThread implements Runnable{

        /** 消费者包装类 */
        private KafkaConsumerWrapper<String, String> consumerWrapper;

        /** 消费者订阅的Topic集合 */
        private final Collection<String> topic;

        /** 拉取的消息集合(以Topic分类) */
        private Map<String, LinkedBlockingQueue<List<ConsumerRecord<String, String>>>> topicRecords
                = new ConcurrentHashMap<String, LinkedBlockingQueue<List<ConsumerRecord<String, String>>>>();

        /**
         * Kafka消息拉取线程构造方法
         *
         * @param consumer
         */
        KafkaMessagePollThread(final KafkaConsumer<String, String> consumer){
            this.consumerWrapper = new KafkaConsumerWrapper<String, String>(consumer);
            topic = new ArrayList<String>();
        }

        /**
         * 订阅Topic
         *
         * @param kafkaConsumerBinding
         */
        synchronized void subscribe(KafkaConsumerBinding kafkaConsumerBinding, ConsumerRebalanceListener listener) {
            // 更新订阅的Topic集合
            topic.add(kafkaConsumerBinding.getConfig().getDestination());
            // 初始化Topic消费线程
            LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue
                    = new LinkedBlockingQueue<List<ConsumerRecord<String, String>>>();
            topicRecords.put(kafkaConsumerBinding.getConfig().getDestination(), topicQueue);
            // 传入封装后的消费者，使其在多线程环境中同步调用
            kafkaConsumerBinding.start(consumerWrapper, topicQueue);
            // 更新拉取的Topic
            consumerWrapper.subscribe(topic, listener);
        }

        @Override
        public void run() {
            // 延迟一定时间等系统启动后再开始消费
            ThreadUtil.sleep(5000, log);

            while (!closeFlag.get()) {
                synchronized (this) {
                    try {
                        // 拉取消息
                        List<List<ConsumerRecord<String, String>>> list = consumerWrapper.sequencePoll(100);
                        if (list.size() > 0) {
                            for (List<ConsumerRecord<String, String>> records : list) {
                                // 将拉取到的消息按Topic分类消费
                                if (records.size() > 0) {
                                    topicRecords.get(records.get(0).topic()).put(records);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        log.warn("Kafka poll message failed.", e);
                    }
                }

                // 休眠300毫秒，减少offset提交时的线程竞争
                ThreadUtil.sleep(300, log);
            }
        }
    }

}
