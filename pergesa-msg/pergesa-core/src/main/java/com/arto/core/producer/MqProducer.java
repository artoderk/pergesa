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

import com.arto.core.common.MessageRecord;
import com.arto.core.exception.MqClientException;

/**
 * 消息中件间客户端通用生产者接口。
 *
 * Created by xiong.j on 2017/1/11.
 */
public interface MqProducer<T> {

    /**
     * 发送消息(简化方法，非事务类消息可以使用直接使用此方法发送)
     *
     * @param message 消息对象
     * @throws MqClientException
     */
    void send(T message) throws MqClientException;

    /**
     * 发送消息(定制发送，可配置发送参数)
     * 注:事务消息必须设置"业务凭证流水号"和"业务类型"，以便发生异常时追踪排错
     *
     * @param record 消息包装对象
     * @throws MqClientException
     */
    void send(MessageRecord<T> record) throws MqClientException;

    /**
     * 发送非事务消息(开启事务发送后，可使用此方法发送非事务消息)
     *
     * @param record 消息包装对象
     * @throws MqClientException
     */
    @Deprecated
    void sendNonTx(MessageRecord<T> record) throws MqClientException;
}
