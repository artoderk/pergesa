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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString
public class Event<T> {

    /** 事件分组 */
    private transient Class group;

    /** 业务凭证流水号 */
    private transient String businessId;

    /** 业务类型 */
    private transient String businessType;

    /** 事件内容 */
    private T payload;

    /** 事件回调 */
    private transient EventCallback<T> callback;

    /** 是否持久化 */
    private transient boolean isPersistent;

    /** 事件重试次数 为-1时无限重试(慎用) */
    private transient int retry;

    /** 消息上下文 */
    private transient EventContext eventContext;
}
