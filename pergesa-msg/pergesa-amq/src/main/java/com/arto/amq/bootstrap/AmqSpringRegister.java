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
package com.arto.amq.bootstrap;

import com.arto.amq.config.AmqConfigManager;
import com.arto.core.exception.MqClientException;
import com.arto.event.util.SpringContextHolder;
import com.google.common.base.Strings;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.jms.support.destination.BeanFactoryDestinationResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 注册Activemq连接池与JmsTemplate
 *
 * Created by xiong.j on 2017/3/21.
 */
@Component
public class AmqSpringRegister {

    @PostConstruct
    public void register(){
        // 服务器地址
        String brokerURL = AmqConfigManager.getString("activemq.bootstrap.servers"
                , "failover:(tcp://localhost:61616)?timeout=10000&randomize=false");
        // 用户名
        String userName = AmqConfigManager.getString("activemq.server.username", "");
        // 密码
        String password = AmqConfigManager.getString("activemq.server.password", "");
        // 最大连接数
        int maxConnections = AmqConfigManager.getInt("activemq.maxConnections", 20);
        // 每个连接最大Session数
        int maximumActiveSessionPerConnection = AmqConfigManager.getInt("activemq.maximumActiveSessionPerConnection", 50);

        if (Strings.isNullOrEmpty(brokerURL)) {
            throw new MqClientException("Parameter 'activemq.bootstrap.servers' can't be null");
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) SpringContextHolder.getBeanFactory();

        /** 注册ActiveMQConnectionFactory */
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ActiveMQConnectionFactory.class);
        builder.addPropertyValue("brokerURL", brokerURL);
        if (Strings.isNullOrEmpty(userName)) {
            builder.addPropertyValue("userName", userName);
            builder.addPropertyValue("password", password);
        }
        if (AmqConfigManager.getBoolean("activemq.useAsyncSend", false)) {
            builder.addPropertyValue("useAsyncSend", true);
        }
        // 优化ACK提交，仅在ack=auto时有效
        if (AmqConfigManager.getBoolean("activemq.optimizeAcknowledge", false)) {
            builder.addPropertyValue("optimizeAcknowledge", true);
            builder.addPropertyValue("optimizeAcknowledgeTimeOut", AmqConfigManager.getInt("activemq.optimizeAcknowledgeTimeOut", 10000));
            builder.addPropertyValue("optimizedAckScheduledAckInterval", AmqConfigManager.getInt("activemq.optimizedAckScheduledAckInterval", 0));
        }
        registry.registerBeanDefinition("amqConnectionFactory", builder.getRawBeanDefinition());

        /** 注册PooledConnectionFactory */
        builder = BeanDefinitionBuilder.genericBeanDefinition(PooledConnectionFactory.class);
        builder.addPropertyValue("maxConnections", maxConnections);
        builder.addPropertyValue("maximumActiveSessionPerConnection", maximumActiveSessionPerConnection);
        builder.addPropertyValue("idleTimeout", AmqConfigManager.getInt("activemq.idleTimeout", 120000));
        builder.addPropertyReference("connectionFactory", "amqConnectionFactory");
        builder.setDestroyMethodName("stop");
        registry.registerBeanDefinition("amqPooledConnectionFactory", builder.getRawBeanDefinition());

        /** 注册BeanFactoryDestinationResolver */
        builder = BeanDefinitionBuilder.genericBeanDefinition(BeanFactoryDestinationResolver.class);
        builder.addConstructorArgValue(SpringContextHolder.getBeanFactory());
        registry.registerBeanDefinition("beanFactoryDestinationResolver", builder.getRawBeanDefinition());

        /** 注册JmsTemplate */
        builder = BeanDefinitionBuilder.genericBeanDefinition(AmqJmsTemplate.class);
        builder.addPropertyReference("connectionFactory", "amqPooledConnectionFactory");
        builder.addPropertyReference("destinationResolver", "beanFactoryDestinationResolver");
        registry.registerBeanDefinition("amqJmsTemplate", builder.getRawBeanDefinition());
    }
}
