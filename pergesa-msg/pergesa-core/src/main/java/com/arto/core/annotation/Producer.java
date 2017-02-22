package com.arto.core.annotation;

import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MqTypeEnum;

import java.lang.annotation.*;

/**
 * Created by xiong.j on 2017/2/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Inherited
public @interface Producer {

    // 消息中间件类型
    MqTypeEnum type() default MqTypeEnum.UNKNOWN;

    // 目的地
    String destination();

    // 优先级
    MessagePriorityEnum priority() default MessagePriorityEnum.HIGH;

    // 异步回调
    Class callback() default Producer.class;

}
