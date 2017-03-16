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
package com.arto.core.annotation;

import java.lang.annotation.*;

/**
 * 事务消息标识，用来加快事务消息发送速度。未加上此标识发送的事务消息将只能由调度任务定时发送。
 *
 * 例：
 * @Producer(destination = "pegesa-test", priority = MessagePriorityEnum.HIGH)
 * private Producer<String, Object> producer;
 *
 * @TxMessage
 * @Transactional
 * public void purchase(){
 *    RepositoryDO repositoryDO= ...;
 *    repositoryService.decreaseRepository(repository);
 *    producer.send(new MessageRecord(businessId, businessType, repositoryDO);
 *    ...
 * }
 *
 * Created by xiong.j on 2016/12/20.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface TxMessage {

}
