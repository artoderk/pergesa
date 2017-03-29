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
package com.arto.amq.producer.binding;

import com.arto.amq.common.AmqConstants;
import com.arto.amq.event.AmqProduceEvent;
import com.arto.amq.util.AmqStringUtil;
import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.DataPipeline;
import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.event.MqEvent;
import com.arto.core.exception.MqClientException;
import com.arto.core.intercepter.TxMessageContextHolder;
import com.arto.core.producer.MqProducer;
import com.arto.event.bootstrap.EventBusFactory;
import com.arto.event.service.PersistentEventService;
import com.arto.event.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生产者绑定类，一个对象对应一个Topic和一份配置
 *
 * Created by xiong.j on 2017/1/12.
 */
@Slf4j
public class AmqProducerBinding implements MqProducer {

    private static final AtomicBoolean closeFlag = new AtomicBoolean(false);

    /** Kafka生产者配置 */
    private final AmqProducerConfig config;

    /** 持久化事件服务 */
    private final PersistentEventService service;

    static {
        // 注册事务消息发送线程
        new Thread(new AmqTxMessageSendThread(), "AmqTxMessageSendThread").start();
    }

    public AmqProducerBinding(AmqProducerConfig config) {
        // 注册目的地
        registerDest(config.getDestination());
        this.config = config;
        // 暂时依赖Spring获取
        this.service = SpringContextHolder.getBean("persistentEventService");
    }

    /**
     * 发送消息(简化方法，非事务类消息可以使用直接使用此方法发送)
     *
     * @param message
     * @throws MqClientException
     */
    @Override
    @SuppressWarnings("unchecked")
    public void send(Object message) throws MqClientException {
        innerSend(new MessageRecord(message), false);
    }

    /**
     * 发送消息(定制发送，可配置发送参数)
     * 注:事务消息必须设置"业务凭证流水号"和"业务类型"，以便发送异常时追踪排错
     *
     * @param record
     * @throws MqClientException
     */
    @Override
    public void send(MessageRecord record) throws MqClientException {
        innerSend(record, config.isTransaction());
    }

    /**
     * 发送非事务消息(开启事务发送后，可使用此方法发送非事务消息)
     *
     * @param record
     * @throws MqClientException
     */
    @Override
    @Deprecated
    public void sendNonTx(MessageRecord record) throws MqClientException {
        innerSend(record, false);
    }

    /**
     * 获取绑定的配置
     *
     * @return
     */
    public AmqProducerConfig getConfig() {
        return config;
    }

    /**
     * 销毁线程
     */
    public void close() {
        if (!closeFlag.get()) {
            synchronized (AmqProducerBinding.class) {
                if (!closeFlag.get()) {
                    closeFlag.set(true);
                }
            }
        }
    }

    private void registerDest(String destination) {
        // 注册TOPIC或QUEUE
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) SpringContextHolder.getBeanFactory();
        BeanDefinitionBuilder builder;
        String destName = AmqStringUtil.getDestName(config.getDestination());
        if (destName.startsWith("T") || destName.startsWith("t")) {
            // 以"T"或"t"开头为TOPIC
            builder = BeanDefinitionBuilder.genericBeanDefinition(ActiveMQTopic.class);
        } else {
            // 以"Q"或"q"开头为QUEUE
            builder = BeanDefinitionBuilder.genericBeanDefinition(ActiveMQQueue.class);
        }
        builder.addConstructorArgValue(destination);
        registry.registerBeanDefinition(destName, builder.getRawBeanDefinition());
    }

    private void innerSend(MessageRecord record, boolean isTransaction) throws MqClientException {
        if (record == null || record.getMessage() == null) {
            throw new MqClientException("Message can't be null or blank");
        }

        // 转换为事件
        AmqProduceEvent event = buildEvent(record, isTransaction);
        if (event.isPersistent()) {
            // 持久化消息直接持久化(模拟客户端两阶段提交)
            service.persist(event, AmqConstants.AMQ_EVENT_BEAN);
            // 加入线程上下文，等待事务正常结束后加入发送Queue处理(避免定时调度的延迟，调度默认10分钟执行一次)
            TxMessageContextHolder.setTxMessage(event);
        } else {
            // 非持久化消息直接发送
            EventBusFactory.getInstance().post(event);
        }
    }

    @SuppressWarnings("unchecked")
    private AmqProduceEvent buildEvent(MessageRecord record, boolean isTransaction){
        AmqProduceEvent event = new AmqProduceEvent();
        // 业务流水号
        event.setBusinessId(record.getBusinessId());
        // 业务类型
        event.setBusinessType(record.getBusinessType());
        // 目的地
        event.setDestination(config.getDestination());
        // 优先级
        event.setPriority(config.getPriority().getCode());
        // 持久化
        event.setPersistent(isTransaction);
        // 消息
        event.setPayload(record);
        // 回调
        event.setCallback(config.getCallback());
        return event;
    }

    /**
     * 持久化消息存储后，直接扔Queue里在此线程处理
     */
    private static class AmqTxMessageSendThread implements Runnable{

        private final DataPipeline<MqEvent> dataPipeline;

        private AmqTxMessageSendThread() {
            dataPipeline = MqClient.getPipeline(MqTypeEnum.ACTIVEMQ.getMemo());
        }

        @Override
        public void run() {
            while (!closeFlag.get()) {
                try {
                    AmqProduceEvent event = (AmqProduceEvent)dataPipeline.poll(300, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        EventBusFactory.getInstance().post(event);
                    }
                } catch (Throwable t) {
                    log.warn("Send message failed.", t);
                }
            }
        }
    }
}
