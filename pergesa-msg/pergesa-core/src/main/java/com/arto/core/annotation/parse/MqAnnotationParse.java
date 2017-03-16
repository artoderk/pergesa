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

import com.arto.core.common.MqTypeEnum;
import com.arto.core.config.MqConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiong.j on 17/2/15.
 */
@Slf4j
public abstract class MqAnnotationParse implements InitializingBean, BeanPostProcessor {

    protected Set<String> topicKeySet = new HashSet<String>();

    protected String defaultType;

    public void afterPropertiesSet() throws Exception {
        defaultType = MqConfigManager.getString("default.mq.type", MqTypeEnum.KAFKA.getMemo());
    }

    protected Object getTargetBean(Object bean) {
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
