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
package com.arto.amq.producer.binding;

import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.producer.ProducerConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/12.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class AmqProducerConfig extends ProducerConfig {

    /** 消息模式 以DEST首字母判断 */
    //private DestTypeEnum destType = DestTypeEnum.QUEUE;

    /************************** 扩展设置 start **************************/
    /** 发送模式 2:持久化，1:非持久化 */
    private int deliveryMode = -1;

    /** 过期时间 */
    private long timeToLive = -1;
    /************************** 扩展设置 end   **************************/

    public AmqProducerConfig(String destination) {
        this.setType(MqTypeEnum.ACTIVEMQ);
        this.setDestination(destination);
    }

    public AmqProducerConfig(String destination, MessagePriorityEnum priority) {
        this(destination);
        this.setPriority(priority);
        if (priority == MessagePriorityEnum.HIGH) {
            this.setTransaction(true);
        }
    }

}
