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
package com.arto.core.annotation.parse;

import com.arto.core.annotation.Consumer;
import com.arto.core.annotation.Producer;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.config.MqConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by xiong.j on 17/2/15.
 */
@Slf4j
public class MqAnnotationParse implements InitializingBean, BeanPostProcessor {

    // 消费者注解去重用
    private Map<String, Set<String>> keySet = new HashMap<String, Set<String>>(3);

    // 注解解析策略
    private Map<String, MqParseStrategy> strategyMap = new HashMap<String, MqParseStrategy>();

    // 默认消息中间件类型
    private String defaultType;

    public void afterPropertiesSet() throws Exception {
        // 获取默认消息中件间类型
        defaultType = MqConfigManager.getString("default.mq.type", MqTypeEnum.KAFKA.getMemo());

        // 加载注解解析实现
        ServiceLoader<MqParseStrategy> serviceLoader = ServiceLoader.load(MqParseStrategy.class);
        Iterator<MqParseStrategy> parseStrategies = serviceLoader.iterator();
        MqParseStrategy parseStrategy;
        while(parseStrategies.hasNext()){
            parseStrategy = parseStrategies.next();
            strategyMap.put(parseStrategy.getMqType(), parseStrategy);

            // 初始化目的地列表
            keySet.put(parseStrategy.getMqType(), new HashSet<String>());
        }
    }

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
        for (Method method : methods) {
            parseConsumer(targetBean, method);
        }
        return bean;
    }

    private void parseProducer(Object bean, Field field) {
        Producer annotation = field.getAnnotation(Producer.class);
        if (annotation != null) {
            getStrategy(annotation.type().getMemo()).parseProducer(bean, field);
        }
    }

    private void parseConsumer(Object bean, Method method) {
        Consumer annotation = method.getAnnotation(Consumer.class);
        if (annotation != null) {
            String type = annotation.type().getMemo();
            getStrategy(type).parseConsumer(keySet.get(type), bean, method);
        }
    }

    private MqParseStrategy getStrategy(String type){
        if (type.equals(MqTypeEnum.UNKNOWN.getMemo())) {
            // 没有标注则取默认实现
            return strategyMap.get(defaultType);
        } else {
            return strategyMap.get(type);
        }
    }

    private Object getTargetBean(Object bean) {
        if (bean instanceof Advised) {
            try {
                return ((Advised) bean).getTargetSource().getTarget();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return bean;
    }

}
