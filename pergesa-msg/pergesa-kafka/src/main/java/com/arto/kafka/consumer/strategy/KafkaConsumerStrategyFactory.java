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
package com.arto.kafka.consumer.strategy;

import com.arto.event.bootstrap.EventBusFactory;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 消费策略工厂
 *
 * Created by xiong.j on 2017/1/20.
 */
public class KafkaConsumerStrategyFactory {

    private static volatile KafkaConsumerStrategyFactory instance;

    private final ConcurrentMap<Integer, SoftReference<KafkaConsumerStrategy>> strategyMap
            = new ConcurrentHashMap<Integer, SoftReference<KafkaConsumerStrategy>>(3);

    public static KafkaConsumerStrategyFactory getInstance(){
        if (null == instance) {
            synchronized (EventBusFactory.class) {
                if (null == instance) {
                    instance = new KafkaConsumerStrategyFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 根据消费优先级生成不同的消费策略
     *
     * @param priority
     * @return
     */
    public KafkaConsumerStrategy getStrategy(final int priority){
        if (strategyMap.containsKey(priority)) {
            if (strategyMap.get(priority) != null) {
                return strategyMap.get(priority).get();
            }
        }
        return createStrategy(priority);
    }

    private KafkaConsumerStrategy createStrategy(final int priority) {
        if (strategyMap.containsKey(priority)) {
            if (strategyMap.get(priority) != null) {
                return strategyMap.get(priority).get();
            }
        }

        KafkaConsumerStrategy strategy;
        switch (priority) {
            case 1:
                // 重要消息，重试三次后入库等待重试
                strategy = new KafkaConsumerDefaultStrategy();
                break;
            case 2:
                // 普通消息，重试三次后丢弃消息
                strategy = new KafkaConsumerMediumPriorityStrategy();
                break;
            case 3:
                // 不重要消息，出错即丢弃消息
                strategy = new KafkaConsumerLowPriorityStrategy();
                break;
            default:
                strategy = new KafkaConsumerDefaultStrategy();
        }
        strategyMap.put(priority, new SoftReference<KafkaConsumerStrategy>(strategy));
        return strategy;
    }

}
