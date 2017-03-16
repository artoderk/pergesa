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

import com.arto.event.bootstrap.Event;
import com.arto.event.bootstrap.EventListener;
import com.arto.event.service.EventAdviceService;
import com.arto.kafka.common.Constants;
import com.arto.kafka.config.KafkaConfigManager;
import com.arto.kafka.event.KafkaProduceEvent;
import com.arto.kafka.producer.KafkaMessageProducer;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 当持久化事件达到重试次数依然失败时发送失败报告消息。
 *
 * Created by xiong.j on 2017/3/2.
 */
@Component
public class KafkaReportEventListener implements EventListener<Event<String>> {

    @Autowired
    private KafkaMessageProducer producer;

    @Autowired
    private EventAdviceService service;

    @Subscribe
    @AllowConcurrentEvents
    @Override
    public void listen(Event<String> event) {
        try {
            // 前处理
            service.before(event);
            // 发送消息
            producer.send(convert2(event));
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

    private KafkaProduceEvent convert2(Event<String> event){
        KafkaProduceEvent produceEvent = new KafkaProduceEvent();
        produceEvent.setDestination(KafkaConfigManager.getString("event.failed.report.dest"
                , com.arto.event.common.Constants.REPORT_DEST));
        produceEvent.setPayload(event.getPayload());
        return produceEvent;
    }

}