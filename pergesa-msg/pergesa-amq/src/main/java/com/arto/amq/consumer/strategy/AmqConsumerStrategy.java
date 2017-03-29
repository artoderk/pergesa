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
package com.arto.amq.consumer.strategy;

import com.arto.amq.consumer.binding.AmqConsumerConfig;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * Amq消费策略接口
 *
 * Created by xiong.j on 2017/3/29.
 */
public interface AmqConsumerStrategy {

    /**
     * 消费消息
     *
     * @param config
     * @param message
     */
    void onMessage(final AmqConsumerConfig config, final TextMessage message) throws JMSException;

}
