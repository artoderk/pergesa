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
package com.arto.amq.consumer.binding;

import com.arto.core.common.MqTypeEnum;
import com.arto.core.consumer.ConsumerConfig;
import com.arto.core.consumer.MqListener;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/12.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class AmqConsumerConfig extends ConsumerConfig {

    /** 批量消费标识大小 */
    private int batchSize = 5;

    public AmqConsumerConfig(String destination) {
        this.setType(MqTypeEnum.ACTIVEMQ);
        this.setDestination(destination);
    }

    public AmqConsumerConfig(String destination, MqListener listener) {
        this(destination);
        this.setListener(listener);
    }

    public AmqConsumerConfig(String destination, MqListener listener, int priority) {
        this(destination, listener);
        this.setPriority(priority);
        this.setListener(listener);
    }
}
