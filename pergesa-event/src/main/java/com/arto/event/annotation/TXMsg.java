package com.arto.event.annotation;

import java.lang.annotation.*;

/**
 *
 * 事务消息
 * 例：
 * @TXMsg
 * private Producer<String, TransactionMessage> producer;
 *
 * public void purchase(){
 *    RepositoryDO repository= ...;
 *    repositoryService.decreaseRepository(repository);
 *    producer.send(new ProducerRecord<String, RepositoryDO>("galaxy-tx-payload", repository));
 *    ...
 * }
 *
 * Created by xiong.j on 2016/12/20.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface TXMsg {
    /**
     * 业务操作类型
     * @return
     */
    String bizType() default "";
    /**
     * 定义分布式事务执行的超时时间,默认不启用
     * @return
     */
    int retry() default -1;

}
