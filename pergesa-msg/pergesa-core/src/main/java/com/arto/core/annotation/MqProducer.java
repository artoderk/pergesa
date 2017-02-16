package com.arto.core.annotation;

import java.lang.annotation.*;

/**
 * Created by xiong.j on 2017/2/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface MqProducer {


}
