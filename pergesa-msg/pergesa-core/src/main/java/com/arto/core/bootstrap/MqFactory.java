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

import com.arto.core.consumer.ConsumerConfig;
import com.arto.core.consumer.MqConsumer;
import com.arto.core.producer.MqProducer;
import com.arto.core.producer.ProducerConfig;

/**
 * Created by xiong.j on 2017/1/11.
 */
public interface MqFactory {

    /**
     * 根据生产者配置文件生成一个新的生产者
     *
     * @param config
     * @return
     */
    MqProducer buildProducer(ProducerConfig config);

    /**
     * 根据消费者配置文件生成一个新的消费者
     *
     * @param config
     * @return
     */
    MqConsumer buildConsumer(ConsumerConfig config);

    /**
     * 获取当前的实例使用的消息中件间类型
     *
     * @return
     */
    String getMqType();

    /**
     * 销毁所有的生产者和消息者
     */
    void destroy();
}
