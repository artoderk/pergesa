package com.arto.core.annotation;

import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MqTypeEnum;

import java.lang.annotation.*;

/**
 * Created by xiong.j on 2017/2/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface Consumer {

    // 消息中间件类型
    MqTypeEnum type() default MqTypeEnum.UNKNOWN;

    // 目的地
    String destination();

    // 选择Key
    @Deprecated
    String selectKey() default "";

    // 优先级
    MessagePriorityEnum priority() default MessagePriorityEnum.HIGH;

    // 去重方法名
    String checkRedeliver() default "";

    // 线程池大小
    int numThreads() default 2;

    // 批量提交消费标识大小
    int ackSize() default 5;

}
