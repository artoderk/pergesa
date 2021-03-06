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
package com.arto.event.service;

import com.arto.event.storage.EventInfo;
import com.arto.event.bootstrap.Event;
import com.arto.event.exception.PersistentEventLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Service("eventAdviceService")
public class EventAdviceServiceImpl implements EventAdviceService {

    @Autowired
    private PersistentEventService persistentEventService;

    /**
     * Event处理前的处理
     *
     * @param event
     * @return
     * @throws
     */
    @Override
    public Event before(Event event) throws Exception{
        if (isPersistentEvent(event)) {
            EventInfo eventInfo = persistentEventService.lock(event.getEventContext().getEventInfo());
            if (eventInfo != null) {
                event.getEventContext().setEventInfo(eventInfo);
            }
        }
        return event;
    }

    /**
     * Event处理后的处理
     *
     * @param event
     */
    @Override
    public void after(Event event){
        log.debug("Process event successful. Event:" + event);
        if (isPersistentEvent(event)) {
            persistentEventService.finish(event.getEventContext().getEventInfo());
        }
    }

    /**
     * Event处理失败时的处理
     *
     * @param event
     * @param throwable
     */
    @Override
    public void fail(Event event, Throwable throwable){
        log.warn("Process event failed. Event:" + event, throwable);
        if (isPersistentEvent(event) && !(throwable instanceof PersistentEventLockException)) {
            // 持久化消息 且 错误类型不为加锁失败时
            persistentEventService.fail(event.getEventContext().getEventInfo());
        }
    }

    private boolean isPersistentEvent(Event event) {
        if (event.getEventContext() != null) {
            if (event.getEventContext().getEventInfo() != null) {
                return true;
            }
        }
        return false;
    }
}
