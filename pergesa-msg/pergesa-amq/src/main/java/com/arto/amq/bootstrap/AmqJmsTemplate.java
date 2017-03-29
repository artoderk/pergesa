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

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;

import javax.jms.*;

/**
 * Created by xiong.j on 2017/3/23.
 */
public class AmqJmsTemplate extends JmsTemplate {

    public AmqJmsTemplate() {
        super();
    }

    public AmqJmsTemplate(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    /**
     * Send message
     *
     * @param destination
     * @param messageCreator
     * @param deliveryMode
     * @param priority
     * @param timeToLive
     * @throws JmsException
     */
    public void send(final Destination destination, final MessageCreator messageCreator
            , final int deliveryMode, final int priority, final long timeToLive) throws JmsException {
        execute(new SessionCallback<Object>() {
            public Object doInJms(Session session) throws JMSException {
                doSend(session, destination, messageCreator, deliveryMode, priority, timeToLive);
                return null;
            }
        }, false);
    }

    /**
     * Send message
     *
     * @param destinationName
     * @param messageCreator
     * @param deliveryMode
     * @param priority
     * @param timeToLive
     * @throws JmsException
     */
    public void send(final String destinationName, final MessageCreator messageCreator
            , final int deliveryMode, final int priority, final long timeToLive) throws JmsException {
        execute(new SessionCallback<Object>() {
            public Object doInJms(Session session) throws JMSException {
                Destination destination = resolveDestinationName(session, destinationName);
                doSend(session, destination, messageCreator, deliveryMode, priority, timeToLive);
                return null;
            }
        }, false);
    }

    /**
     * Send the given JMS message.
     *
     * @param session
     * @param destination
     * @param messageCreator
     * @param deliveryMode
     * @param priority
     * @param timeToLive
     * @throws JMSException
     */
    private void doSend(Session session, Destination destination, MessageCreator messageCreator
            , int deliveryMode, int priority, long timeToLive) throws JMSException {

        Assert.notNull(messageCreator, "MessageCreator must not be null");
        MessageProducer producer = createProducer(session, destination);
        try {
            Message message = messageCreator.createMessage(session);
            if (logger.isDebugEnabled()) {
                logger.debug("Sending created message: " + message);
            }
            doSend(producer, message, deliveryMode, priority, timeToLive);
            // Check commit - avoid commit call within a JTA transaction.
            if (session.getTransacted() && isSessionLocallyTransacted(session)) {
                // Transacted session created by this template -> commit.
                JmsUtils.commitIfNecessary(session);
            }
        }
        finally {
            JmsUtils.closeMessageProducer(producer);
        }
    }

    /**
     * Actually send the given JMS message.
     *
     * @param producer
     * @param message
     * @param deliveryMode
     * @param priority
     * @param timeToLive
     * @throws JMSException
     */
    private void doSend(MessageProducer producer, Message message
            , int deliveryMode, int priority, long timeToLive) throws JMSException {
        if (deliveryMode != -1 || priority != -1 || timeToLive != -1) {
            producer.send(message, deliveryMode, priority, timeToLive);
        } else {
            producer.send(message);
        }
    }
}
