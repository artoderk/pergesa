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
package com.arto.amq.consumer;

import com.arto.amq.config.AmqConfigManager;
import com.arto.amq.consumer.binding.AmqConsumerBinding;
import com.arto.amq.consumer.binding.AmqConsumerConfig;
import com.arto.amq.util.AmqStringUtil;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.exception.MqClientException;
import com.arto.event.util.SpringContextHolder;
import com.arto.event.util.SpringThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiong.j on 2017/3/20.
 */
@Slf4j
@Component("amqMessageConsumer")
public class AmqMessageConsumer {

    private Map<String, AmqConsumerConfig> configMap = new HashMap<String, AmqConsumerConfig>();

    @Autowired
    @Qualifier("amqPooledConnectionFactory")
    private PooledConnectionFactory pooledConnectionFactory;

    public AmqConsumerConfig getDestConfig(String destName) {
        return configMap.get(destName);
    }

    public synchronized void subscribe(final AmqConsumerBinding amqConsumerBinding) {
        AmqConsumerConfig config = amqConsumerBinding.getConfig();
        if(configMap.containsKey(config.getDestination())) {
            throw new MqClientException("Duplicated consumer definition. config:" + config);
        } else {
            configMap.put(config.getDestination(), config);
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) SpringContextHolder.getBeanFactory();
        /** 注册messageListener */
        String beanName = "amqMessageListener" + configMap.size();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AmqMessageListener.class);
        builder.addPropertyReference("messageConsumer", "amqMessageConsumer");
        registry.registerBeanDefinition(beanName, builder.getRawBeanDefinition());

        /** 注册MessageListenerAdapter */
        builder = BeanDefinitionBuilder.genericBeanDefinition(MessageListenerAdapter.class);
        builder.addConstructorArgReference(beanName);
        builder.addPropertyValue("messageConverter", null);
        beanName = "amqMessageListenerAdapter" + configMap.size();
        registry.registerBeanDefinition(beanName, builder.getRawBeanDefinition());

        /** 注册Destination */
        String destName = AmqStringUtil.getDestName(config.getDestination());
        boolean isPubSubDomain = destName.startsWith("T");
        Destination destination = SpringContextHolder.getBean(destName);
        if (destination == null) {
            if (isPubSubDomain) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(ActiveMQTopic.class);
            } else {
                builder = BeanDefinitionBuilder.genericBeanDefinition(ActiveMQQueue.class);
                if (config.getPriority() != MessagePriorityEnum.HIGH.getCode()) {
                    // 中低优先级消息批量消费批量确认
                    config.setDestination(AmqStringUtil.addParamToDest(config.getDestination()
                            , "consumer.prefetchSize=" + config.getBatchSize()));
                } else {
                    // 高优先级消息逐条拉取与确认
                    config.setDestination(AmqStringUtil.addParamToDest(config.getDestination()
                            , "consumer.prefetchSize=" + 1));
                }
            }
            builder.addConstructorArgValue(config.getDestination());
        }

        /** 注册DefaultMessageListenerContainer */
        ThreadPoolTaskExecutor taskExecutor = null;
        builder = BeanDefinitionBuilder.genericBeanDefinition(DefaultMessageListenerContainer.class);
        builder.addPropertyReference("connectionFactory", "amqPooledConnectionFactory");
        builder.addPropertyReference("destination", destName);
        builder.addPropertyReference("messageListener", beanName);
        // 高优先级或TOPIC类消息确保一个Session只有一个消费者，TOPIC永远AutoACK
        if (config.getPriority() == MessagePriorityEnum.HIGH.getCode() || isPubSubDomain) {
            taskExecutor = SpringThreadPoolUtil.getNewPool(beanName
                    , AmqConfigManager.getInt("amq.producer.pool.coreSize", 1)
                    , AmqConfigManager.getInt("amq.producer.pool.maxSize", 1)
                    , AmqConfigManager.getInt("amq.producer.pool.queueCapacity", 10)
                    , null);
            builder.addPropertyValue("concurrentConsumers", 1);
            if (!isPubSubDomain) {
                builder.addPropertyValue("SessionAcknowledgeMode", Session.SESSION_TRANSACTED);
            }
        } else {
            // 中低优先级消息批量消费批量确认
            int coreSize = (int)Math.ceil(config.getNumThreads() * 1.3);
            taskExecutor = SpringThreadPoolUtil.getNewPool(beanName
                    , AmqConfigManager.getInt("amq.producer.pool.coreSize", config.getNumThreads())
                    , AmqConfigManager.getInt("amq.producer.pool.maxSize", (int)Math.ceil(coreSize * 1.5))
                    , AmqConfigManager.getInt("amq.producer.pool.queueCapacity", 999)
                    , null);
            builder.addPropertyValue("concurrentConsumers", config.getNumThreads());
            builder.addPropertyValue("SessionAcknowledgeMode", Session.AUTO_ACKNOWLEDGE);
        }
        builder.addPropertyReference("taskExecutor", taskExecutor.getThreadNamePrefix());
        beanName = "amqMessageListenerContainer" + configMap.size();
        registry.registerBeanDefinition(beanName, builder.getRawBeanDefinition());
    }

}
