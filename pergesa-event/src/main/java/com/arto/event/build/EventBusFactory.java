/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.arto.event.build;

import com.arto.event.exception.EventProcessException;
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
        String group = event.getClass().getName();
        if (itemMap.containsKey(group)) {
            itemMap.get(group).post(event);
        } else {
            throw new EventProcessException("This event has no EventBus instance.");
        }
    }

    /**
     * 清除监听器.
     *
     * @param group 分组
     */
    public synchronized void clearListeners(final String group) {
        if (itemMap.containsKey(group)) {
            itemMap.get(group).clearListeners();
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
                throw new EventProcessException("This event has no listeners.");
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
