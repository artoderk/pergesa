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

import com.arto.event.bootstrap.EventListener;
import com.arto.event.service.EventAdviceService;
import com.arto.kafka.common.Constants;
import com.arto.kafka.event.KafkaProduceEvent;
import com.arto.kafka.producer.KafkaMessageProducer;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息发送事件
 *
 * Created by xiong.j on 2017/1/6.
 */
@Component
public class KafkaProduceEventListener implements EventListener<KafkaProduceEvent> {

    @Autowired
    private KafkaMessageProducer producer;

    @Autowired
    private EventAdviceService service;

    @Subscribe
    @AllowConcurrentEvents
    @Override
    public void listen(KafkaProduceEvent event) {
        try {
            // 前处理
            service.before(event);
            // 发送消息
            producer.send(event);
            // 后处理
            service.after(event);
        } catch (Throwable e) {
            // 失败处理
            service.fail(event, e);
        }
    }

    @Override
    public String getIdentity() {
        return Constants.KAFKA;
    }

}
