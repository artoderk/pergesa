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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString
public class EventInfo {

    private long id = -1L;

    private int tag = -1;

    /** (子)系统ID */
    private String systemId;

    /** 业务凭证流水号 */
    private String businessId;

    /** 业务类型 */
    private String businessType;

    /** 事件类型 */
    private String eventType;

    /** 处理状态 */
    private int status = -1;

    /** 事件内容 */
    private String payload;

    /** 下次重试时间 */
    private Timestamp nextRetryTime;

    /** 默认重试次数 */
    private int defaultRetriedCount = -1;

    /** 当前重试次数 */
    private int currentRetriedCount;

    /** 备注 */
    private String memo;

    /** 创建时间 */
    private Timestamp gmtCreated;

    /** 更新时间 */
    private Timestamp gmtModified;

}
