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
package com.arto.core.annotation;

import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MqTypeEnum;

import java.lang.annotation.*;

/**
 * Created by xiong.j on 2017/2/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Inherited
public @interface Producer {

    // 消息中间件类型
    MqTypeEnum type() default MqTypeEnum.UNKNOWN;

    // 目的地
    String destination();

    // 优先级
    MessagePriorityEnum priority() default MessagePriorityEnum.HIGH;

    // 异步回调 默认为null，以"Producer.class"代替null
    Class callback() default Producer.class;

    // 是否启用消息两阶段提交
    boolean isTransaction() default true;

}
