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
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface Consumer {

    // 消息中间件类型
    MqTypeEnum type() default MqTypeEnum.UNKNOWN;

    // 目的地
    String destination();

    // 选择Key
    @Deprecated
    String selectKey() default "";

    // 优先级
    MessagePriorityEnum priority() default MessagePriorityEnum.HIGH;

    // 去重方法名
    String checkRedeliver() default "";

    // 线程池大小
    int numThreads() default 2;

    // 批量消费标识大小
    int batchSize() default 5;

}
