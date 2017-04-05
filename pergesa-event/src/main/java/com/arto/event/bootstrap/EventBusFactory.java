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
package com.arto.event.bootstrap;

import com.arto.event.exception.EventException;
import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线.
 * 
 * Created by xiongjie on 2016/12/27.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventBusFactory {

    private static volatile EventBusFactory instance;

    private static final String DEFAULT_GROUP = "default";

    private static final ConcurrentHashMap<String, EventBusInstance> itemMap = new ConcurrentHashMap<String, EventBusInstance>();

    private static final String version = "1.0.0";

    static {
        log.info("pergesa-event current version:" + version);
    }

    public static EventBusFactory getInstance() {
        if (null == instance) {
            synchronized (EventBusFactory.class) {
                if (null == instance) {
                    instance = new EventBusFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 注册事件监听器.
     *
     * @param group    作业名
     * @param listener 作业事件
     */
    public synchronized void register(final Class group, final EventListener listener) {
        if (group == null || listener == null) throw new EventException("Group and Listener can't be null.");;
        if (!itemMap.containsKey(group.getName())) {
            itemMap.putIfAbsent(group.getName(), new EventBusInstance());
        }
        itemMap.get(group.getName()).register(listener);
    }

    /**
     * 发布事件.
     *
     * @param event 事件
     */
    public void post(final Event event) {
        if (event == null) throw new EventException("Event can't be null.");
        String group;
        if (event.getGroup() != null) {
            // 有设Group的直接使用Group
            group = event.getGroup().getName();
        } else {
            // 无设Group的取Class名
            group = event.getClass().getName();
        }
        if (itemMap.containsKey(group)) {
            itemMap.get(group).post(event);
        } else {
            throw new EventException("This event has no EventBus instance.");
        }
    }

    /**
     * 清除监听器.
     *
     * @param group 分组
     */
    public synchronized void clearListeners(final Class group) {
        if (group == null) throw new EventException("Group can't be null.");;
        if (itemMap.containsKey(group.getName())) {
            itemMap.get(group.getName()).clearListeners();
        }
    }

    @RequiredArgsConstructor
    private class EventBusInstance {

        private final EventBus eventBus = new EventBus();

        private final ConcurrentHashMap<String, EventListener> listeners = new ConcurrentHashMap<String, EventListener>();


        void register(final EventListener listener) {
            if (null != listener && null == listeners.putIfAbsent(listener.getIdentity(), listener)) {
                eventBus.register(listener);
            }
        }

        void post(final Object event) {
            if (!listeners.isEmpty()) {
                eventBus.post(event);
            } else {
                throw new EventException("This event has no listeners.");
            }
        }

        void clearListeners() {
            for (Object each : listeners.values()) {
                eventBus.unregister(each);
            }
            listeners.clear();
        }
    }
}
