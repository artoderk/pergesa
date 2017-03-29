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
package com.arto.amq.consumer.binding;

import com.arto.core.consumer.MqConsumer;
import com.arto.core.consumer.MqListener;
import com.arto.event.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xiong.j on 2017/1/17.
 */
@Slf4j
public class AmqConsumerBinding implements MqConsumer {

    /** Amq消费者配置 */
    private final AmqConsumerConfig config;

    /** Amq消费者 */
    private final Object consumer;

    /** 消费线程关闭Flag */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    public AmqConsumerBinding(AmqConsumerConfig config) {
        this.config = config;
        this.consumer = SpringContextHolder.getBean("kafkaMessageConsumer");
    }

    @Override
    @Deprecated
    public void receive(Class type, MqListener listener) {
        config.setListener(listener);
    }

    @Override
    @Deprecated
    public void receiveWithParallel(Class type, int numThreads, MqListener listener) {
        config.setListener(listener);
        config.setNumThreads(numThreads);
    }

    public void close(){}
    /**
     * 获取绑定的配置
     *
     * @return
     */
    public AmqConsumerConfig getConfig() {
        return config;
    }




}
