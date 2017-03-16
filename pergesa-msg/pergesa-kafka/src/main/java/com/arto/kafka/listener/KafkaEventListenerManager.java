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
package com.arto.kafka.listener;

import com.arto.event.bootstrap.EventBusFactory;
import com.arto.kafka.event.KafkaConsumeEvent;
import com.arto.kafka.event.KafkaProduceEvent;
import com.arto.kafka.event.KafkaReportEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by xiong.j on 2017/1/6.
 */
@Component
public class KafkaEventListenerManager {

    /** 消息需发送事件监听器 */
    @Autowired
    private KafkaProduceEventListener kafkaProduceEventListener;

    /** 持久件事件报告监听器 */
    @Autowired
    private KafkaReportEventListener kafkaReportEventListener;

    /** 消息需消费事件监听器 */
    @Autowired
    private KafkaConsumeEventListener kafkaConsumeEventListener;

    @PostConstruct
    public void init() throws Exception {
        // 注册事件
        EventBusFactory.getInstance().register(KafkaProduceEvent.class, kafkaProduceEventListener);
        EventBusFactory.getInstance().register(KafkaReportEvent.class, kafkaReportEventListener);
        EventBusFactory.getInstance().register(KafkaConsumeEvent.class, kafkaConsumeEventListener);
    }
}
