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
package com.arto.event.recovery;

import com.arto.event.config.ConfigManager;
import com.arto.event.router.PersistentEventDispatch;
import com.arto.event.storage.EventInfo;
import com.arto.event.storage.EventStorage;
import com.arto.event.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 持久化事件恢复服务
 * 默认恢复7天前的数据，新事件默认延迟10分钟, 一次默认取1000条
 *
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Service
public class EventRecoveryServiceImpl implements EventRecoveryService {

    @Autowired
    private EventStorage eventStorage;

    @Autowired
    private PersistentEventDispatch persistentEventDispatch;

    @Override
    public List<EventInfo> fetchData(List<Integer> tags) {
        // id 升序(默认恢复7天前的数据，新事件默认延迟10分钟, 一次默认取1000条)
        return eventStorage.findSince(ConfigManager.getString("sar.name", "webapp")
                , tags
                , DateUtil.getPrevDayTimestamp(ConfigManager.getInt("event.recovery.start.day", 7))
                , DateUtil.getPrevSecTimestamp(ConfigManager.getInt("event.recovery.delay.second", 600))
                , ConfigManager.getInt("event.recovery.limit", 1000));
    }

    public int execute(List<EventInfo> infos) {
        int successCount = 0;
        for(EventInfo info : infos) {
            persistentEventDispatch.router(info);
            successCount++;
        }
        log.info("Event recovery service executed, data count:" + infos.size() + ", success count:" + successCount);
        return successCount;
    }
}
