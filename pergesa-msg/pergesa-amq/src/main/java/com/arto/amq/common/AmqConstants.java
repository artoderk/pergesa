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
package com.arto.amq.common;

import com.arto.amq.event.AmqConsumeEvent;
import com.arto.amq.event.AmqProduceEvent;

/**
 * Created by xiong.j on 2017/3/21.
 */
public interface AmqConstants {

    /** 消息中间件amq */
    String AMQ = "activemq";

    /** 消息中间件kafka发送消息事件类 */
    String AMQ_EVENT_BEAN = AmqProduceEvent.class.getName();

    /** 消息中间件kafka消费消息标识 */
    String A_CONSUME = "A02";

    /** 消息中间件kafka消费消息事件类 */
    String A_CONSUME_EVENT_BEAN = AmqConsumeEvent.class.getName();

    /** 去除字符*/
    String[] DEST_REMOVE = {".", "-"};
}