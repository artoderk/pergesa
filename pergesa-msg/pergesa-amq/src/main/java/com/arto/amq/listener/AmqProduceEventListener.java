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
package com.arto.amq.listener;

import com.arto.amq.common.AmqConstants;
import com.arto.amq.event.AmqProduceEvent;
import com.arto.amq.producer.AmqMessageProducer;
import com.arto.event.bootstrap.EventListener;
import com.arto.event.service.EventAdviceService;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息发送事件
 *
 * Created by xiong.j on 2017/3/22.
 */
@Component
public class AmqProduceEventListener implements EventListener<AmqProduceEvent> {

    @Autowired
    private AmqMessageProducer producer;

    @Autowired
    private EventAdviceService service;

    @Subscribe
    @AllowConcurrentEvents
    @Override
    public void listen(AmqProduceEvent event) {
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
        return AmqConstants.AMQ;
    }

}
