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
package com.arto.event.util;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * 将线程池托管到Spring,方便监控与销毁
 *
 * Created by xiong.j on 2017/3/16.
 */
public class SpringThreadPoolUtil {

    public static ThreadPoolTaskExecutor getNewPool(String name, int corePoolSize, int maxPoolSize
            , int queueCapacity, RejectedExecutionHandler rejectedExecutionHandler){
        String rejectedBeanName = name + "-" + "ThreadPoolHandler";
        BeanDefinitionBuilder rejectedBeanDefinitionBuilder;
        if (rejectedExecutionHandler == null) {
            rejectedBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy.class);
        } else {
            rejectedBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(rejectedExecutionHandler.getClass());
        }
        BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry)SpringContextHolder.getBeanFactory();
        definitionRegistry.registerBeanDefinition(name + "-" + "ThreadPoolHandler", rejectedBeanDefinitionBuilder.getRawBeanDefinition());

        String poolBeanName = getPoolName(name);
        BeanDefinitionBuilder poolBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ThreadPoolTaskExecutor.class);
        poolBeanDefinitionBuilder.addPropertyValue("corePoolSize", corePoolSize);
        poolBeanDefinitionBuilder.addPropertyValue("maxPoolSize", maxPoolSize);
        poolBeanDefinitionBuilder.addPropertyValue("queueCapacity", queueCapacity);
        poolBeanDefinitionBuilder.addPropertyReference("rejectedExecutionHandler", rejectedBeanName);
        definitionRegistry.registerBeanDefinition(poolBeanName, poolBeanDefinitionBuilder.getRawBeanDefinition());
        return SpringContextHolder.getBean(poolBeanName);
    }

    public static String getPoolName(String name){
        return name + "-" + "ThreadPool";
    }
}
