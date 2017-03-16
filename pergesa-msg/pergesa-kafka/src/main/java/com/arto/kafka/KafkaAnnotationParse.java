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
package com.arto.kafka;

import com.arto.core.annotation.Consumer;
import com.arto.core.annotation.Producer;
import com.arto.core.annotation.parse.MqAnnotationParse;
import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.exception.MqClientException;
import com.arto.core.producer.MqCallback;
import com.arto.core.producer.MqProducer;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.producer.binding.KafkaProducerConfig;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by xiong.j on 2017/2/17.
 */
@Slf4j
@Component
public class KafkaAnnotationParse extends MqAnnotationParse {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Object targetBean = getTargetBean(bean);

        // 扫描@Producer注解
        Field fields[] = targetBean.getClass().getDeclaredFields();
        for (Field field : fields) {
            parseProducer(targetBean, field);
        }

        // 扫描@Consumer注解
        Method[] methods = targetBean.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            parseConsumer(targetBean, methods[i]);
        }
        return bean;
    }

    private void parseProducer(Object bean, Field field) {
        Producer annotation = field.getAnnotation(Producer.class);
        if (annotation != null) {
            if (isKafka(annotation.type())) {
                String topic = StringUtils.trimWhitespace(annotation.destination());
                if (Strings.isNullOrEmpty(topic)) {
                    throw new MqClientException("@Producer's [topic] is required [" + field + "]");
                }

                // 实例化Callback
                MqCallback callback = null;
                Class clz = annotation.callback();
                if (!clz.equals(Producer.class)) {
                    //用构造函数初始化内部类
                    Constructor c = clz.getDeclaredConstructors()[0];
                    //将c设置成可访问
                    c.setAccessible(true);
                    try {
                        callback = (MqCallback) c.newInstance(bean);
                    } catch (Exception e) {
                        throw new MqClientException("@Producer's [callback] can't initialzation.");
                    }
                }

                // 绑定生产者
                KafkaProducerConfig config = new KafkaProducerConfig(topic, annotation.priority());
                if (!clz.equals(Producer.class)) {
                    config.setCallback(callback);
                }
                // 高优先级事务设为true
                if (annotation.priority() == MessagePriorityEnum.HIGH) {
                    config.setTransaction(true);
                }
                try {
                    MqProducer mqProducer = MqClient.buildProducer(config);
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, bean, mqProducer);
                } catch (Throwable t) {
                    throw new MqClientException("Create producer failed. config:" + config, t);
                }
            }
        }
    }

    private void parseConsumer(Object bean, Method method) {
        Consumer annotation = method.getAnnotation(Consumer.class);
        if (annotation != null) {
            if (isKafka(annotation.type())) {
                // 主题
                String topic = StringUtils.trimWhitespace(annotation.destination());
                if (Strings.isNullOrEmpty(topic)) {
                    throw new MqClientException("@Consumer's [topic] is required [" + method + "]");
                }
                // 选择器
                String selectKey = StringUtils.trimWhitespace(annotation.selectKey());

                Type[] types = method.getGenericParameterTypes();
                if (types.length != 1 && !MessageRecord.class.isAssignableFrom(types[0].getClass())) {
                    throw new MqClientException("@Consumer's method [" + method + "] should only have 1 parameter and which type supposed to be MessageRecord<?>");
                }
                if (!method.getParameterTypes()[0].equals(MessageRecord.class)) {
                    throw new MqClientException("@Consumer's method [" + method + "] should only to be MessageRecord<?> ");
                }

                // 重复注解
                String str = MqTypeEnum.KAFKA.getMemo() + topic + selectKey;
                if (topicKeySet.contains(str) || topicKeySet.contains(MqTypeEnum.KAFKA.getMemo() + topic)) {
                    throw new MqClientException("Duplicated definition: @Consumer(type=kafka" + "', topic='" + topic + "', selectKey='" + selectKey + "')");
                }

                // 线程池大小
                int numThreads = annotation.numThreads();
                if (numThreads <= 0) {
                    throw new MqClientException("@Consumer's [numThreads] is invalid.");
                }

                // 批量提交消费标识大小
                int ackSize = annotation.ackSize();
                if (ackSize <= 0) {
                    throw new MqClientException("@Consumer's [ackSize] is invalid.");
                }

                // 去重检测
                String checkRedeliver = annotation.checkRedeliver();

                // 绑定消费者
                KafkaConsumerConfig config = new KafkaConsumerConfig(topic);
                config.setPriority(annotation.priority().getCode());
                config.setBean(bean);
                config.setMethod(method);
                config.setCheckRedeliver(checkRedeliver);
                config.setNumThreads(numThreads);
                config.setAckSize(ackSize);
                try {
                    MqClient.buildConsumer(config);
                } catch (Throwable t) {
                    throw new MqClientException("Create consumer failed. config:" + config, t);
                }
            }
        }
    }

    private boolean isKafka(MqTypeEnum type) {
        return type.equals(MqTypeEnum.KAFKA)
                || (type.equals(MqTypeEnum.UNKNOWN) && this.defaultType.equals(MqTypeEnum.KAFKA.getMemo()));
    }
}
