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

import com.arto.amq.bootstrap.AmqSpringRegister;
import com.arto.amq.config.AmqConfigManager;
import com.arto.amq.consumer.binding.AmqConsumerBinding;
import com.arto.amq.consumer.binding.AmqConsumerConfig;
import com.arto.amq.util.AmqUtil;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.exception.MqClientException;
import com.arto.event.util.SpringContextHolder;
import com.arto.event.util.SpringThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用Spring注册消息监听容器
 *
 * Created by xiong.j on 2017/3/20.
 */
@Slf4j
@Component("amqMessageConsumer")
public class AmqMessageConsumer {

    private Map<String, AmqConsumerConfig> configMap = new HashMap<String, AmqConsumerConfig>();

    @Autowired
    private AmqSpringRegister amqSpringRegister;

    /**
     * 获取Destination对应的配置信息
     *
     * @param destName
     * @return
     */
    AmqConsumerConfig getDestConfig(String destName) {
        return configMap.get(destName);
    }

    /**
     * 注册消息监听容器
     *
     * @param amqConsumerBinding
     */
    public synchronized void subscribe(final AmqConsumerBinding amqConsumerBinding) {
        AmqConsumerConfig config = amqConsumerBinding.getConfig();

        if(configMap.containsKey(config.getDestination())) {
            throw new MqClientException("Duplicated consumer definition. config:" + config);
        } else {
            configMap.put(config.getDestination(), config);
        }

        boolean isPubSubDomain = AmqUtil.isPubSubDomain(config.getDestination());

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) SpringContextHolder.getBeanFactory();

        /** 注册MessageListener */
        String messageListener = registerMessageListener(registry);

        /** 注册MessageListenerAdapter */
        String messageListenerAdapter = registerMessageListenerAdapter(registry, messageListener);

        /** 注册Destination */
        String destination = registerDestination(registry, config, isPubSubDomain);

        /** 注册DefaultMessageListenerContainer */
        registerMessageListenerContainer(registry, config, messageListenerAdapter, destination, isPubSubDomain);
    }

    private String registerMessageListener(BeanDefinitionRegistry registry){
        String beanName = "amqMessageListener" + configMap.size();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AmqMessageListener.class);
        builder.addPropertyReference("messageConsumer", "amqMessageConsumer");
        registry.registerBeanDefinition(beanName, builder.getRawBeanDefinition());
        return beanName;
    }

    private String registerMessageListenerAdapter(BeanDefinitionRegistry registry, String messageListener){
        String beanName = "amqMessageListenerAdapter" + configMap.size();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MessageListenerAdapter.class);
        builder.addConstructorArgReference(messageListener);
        builder.addPropertyValue("messageConverter", null);
        registry.registerBeanDefinition(beanName, builder.getRawBeanDefinition());
        return beanName;
    }

    private String registerDestination(BeanDefinitionRegistry registry, AmqConsumerConfig config, boolean isPubSubDomain){
        String destName = AmqUtil.getDestName(config.getDestination());
        Destination destination;
        try {
            destination = SpringContextHolder.getBean(destName);
        } catch (NoSuchBeanDefinitionException e) {
            destination = null;
        }
        if (destination == null) {
            BeanDefinitionBuilder builder;
            if (isPubSubDomain) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(ActiveMQTopic.class);
            } else {
                builder = BeanDefinitionBuilder.genericBeanDefinition(ActiveMQQueue.class);
                if (config.getPriority() != MessagePriorityEnum.HIGH.getCode()) {
                    // 中低优先级消息批量消费批量确认
                    config.setDestination(AmqUtil.addParamToDest(config.getDestination()
                            , "consumer.prefetchSize=" + config.getBatchSize()));
                } else {
                    // 高优先级消息逐条拉取与确认
                    config.setDestination(AmqUtil.addParamToDest(config.getDestination()
                            , "consumer.prefetchSize=" + 1));
                }
            }
            builder.addConstructorArgValue(config.getDestination());
            registry.registerBeanDefinition(destName, builder.getRawBeanDefinition());
        }
        return destName;
    }

    private String registerMessageListenerContainer(BeanDefinitionRegistry registry, AmqConsumerConfig config
            , String messageListenerAdapter, String destination, boolean isPubSubDomain){
        String beanName = "amqMessageListenerContainer" + configMap.size();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DefaultMessageListenerContainer.class);
        builder.setInitMethodName("initialize");
        builder.setDestroyMethodName("stop");
        builder.addPropertyReference("connectionFactory", "amqPooledConnectionFactory");
        builder.addPropertyReference("destination", destination);
        builder.addPropertyReference("messageListener", messageListenerAdapter);
        builder.addPropertyValue("receiveTimeout", AmqConfigManager.getInt("activemq.receiveTimeout", 1000));

        // 高优先级QUEUE或TOPIC类消息确保一个Session只有一个消费者，TOPIC永远AutoACK
        if (config.getPriority() == MessagePriorityEnum.HIGH.getCode() || isPubSubDomain) {
            SpringThreadPoolUtil.getNewPool(beanName
                    , 1
                    , 1
                    , 10
                    , null);
            builder.addPropertyValue("concurrentConsumers", 1);
            if (!isPubSubDomain) {
                builder.addPropertyValue("sessionTransacted", true );
                builder.addPropertyValue("SessionAcknowledgeMode", Session.SESSION_TRANSACTED);
            }
        } else {
            // 中低优先级QUEUE消息批量消费批量确认
            int coreSize = (int)Math.ceil(config.getNumThreads() * 1.3);
            SpringThreadPoolUtil.getNewPool(beanName
                    , config.getNumThreads()
                    , (int)Math.ceil(coreSize * 1.5)
                    , 999
                    , null);
            builder.addPropertyValue("concurrentConsumers", config.getNumThreads());
            builder.addPropertyValue("SessionAcknowledgeMode", Session.AUTO_ACKNOWLEDGE);
        }
        builder.addPropertyReference("taskExecutor", SpringThreadPoolUtil.getPoolName(beanName));
        registry.registerBeanDefinition(beanName, builder.getRawBeanDefinition());

        DefaultMessageListenerContainer container = SpringContextHolder.getBean(beanName);
        container.start();
        return beanName;
    }
}
