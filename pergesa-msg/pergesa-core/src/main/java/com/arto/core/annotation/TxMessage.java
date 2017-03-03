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
