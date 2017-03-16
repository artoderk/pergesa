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

        String poolBeanName = name + "-" + "ThreadPool";
        BeanDefinitionBuilder poolBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ThreadPoolTaskExecutor.class);
        poolBeanDefinitionBuilder.addPropertyValue("corePoolSize", corePoolSize);
        poolBeanDefinitionBuilder.addPropertyValue("maxPoolSize", maxPoolSize);
        poolBeanDefinitionBuilder.addPropertyValue("queueCapacity", queueCapacity);
        poolBeanDefinitionBuilder.addPropertyReference("rejectedExecutionHandler", rejectedBeanName);
        definitionRegistry.registerBeanDefinition(poolBeanName, poolBeanDefinitionBuilder.getRawBeanDefinition());
        return SpringContextHolder.getBean(poolBeanName);
    }
}
