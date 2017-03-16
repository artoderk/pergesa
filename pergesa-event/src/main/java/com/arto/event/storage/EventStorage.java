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
package com.arto.event.storage;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by xiong.j on 2016/12/21.
 */
public interface EventStorage {

    EventInfo create(EventInfo eventInfo) throws SQLException;

    /** 普通更新操作 */
    int update(EventInfo eventInfo);

    /** 以更新时间来进行乐观更新操作 */
    int optimisticUpdate(EventInfo eventInfo);

    int delete(EventInfo eventInfo);

    EventInfo findById(long id);

    EventInfo lockById(long id);

    @Deprecated
    List<EventInfo> lock(EventInfo eventInfo);

    List<EventInfo> find(EventInfo eventInfo);

    /**
     * 事务恢复任务使用的查询SQL
     *
     * @param systemId 系统ID
     * @param tags 分区
     * @param recoveryDate 恢复时间
     * @param delaySecond 延迟时间
     * @param limit 限制
     * @return 事件列表
     */
    List<EventInfo> findSince(String systemId, List<Integer> tags, Timestamp recoveryDate, Timestamp delaySecond, int limit);

}
