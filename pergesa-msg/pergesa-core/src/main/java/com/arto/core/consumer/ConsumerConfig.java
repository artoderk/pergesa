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
package com.arto.core.consumer;

import com.arto.core.bootstrap.MqConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * Created by xiong.j on 2017/1/11.
 */
@Setter
@Getter
@ToString
public class ConsumerConfig extends MqConfig{

    /** 消息类型反序列化类 */
//    @Deprecated
//    private Class deserializer;

    /** 消费优先级
     * 1:重要消息(单条消息处理完成后消费标识同步提交，为了避免阻塞后续消息，消息处理出错 > 3次后该消息入库，等待调度任务重试处理)
     * 2:普通消息(单条消息处理完成后消费标识同步提交，消息处理出错 > 3次后?)
     * 3:不重要消息(消费标识异步提交, 处理出错后将会丢失该条消息) */
    private int priority = 1;

    /** 消息处理类 */
    private MqListener listener;

    /** 注解所在类实例(使用注解时代替MqListener) */
    private Object bean;

    /** 注解所在方法(使用注解时代替MqListener) */
    private Method method;

    /** 去重方法名(使用注解时代替MqListener) */
    private String checkRedeliver;

    /** 并发线程数量 */
    private int numThreads = 2;

}
