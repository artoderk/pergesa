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
package com.arto.core.producer;

import com.arto.core.bootstrap.MqConfig;
import com.arto.core.common.MessagePriorityEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/11.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class ProducerConfig extends MqConfig {

    /** 消息优先级 1:事务消息 2:非事务重要消息 3:普通消息 */
    private MessagePriorityEnum priority = MessagePriorityEnum.HIGH;

    /** 发送完成后的回调，优先级"1:事务消息"时禁止设置 */
    private MqCallback callback;

    /** 是否启用消息二阶段提交，启用后确保DB和消息中间件的原子性, 目前采用客户端模拟方案 */
    private boolean isTransaction;

}

