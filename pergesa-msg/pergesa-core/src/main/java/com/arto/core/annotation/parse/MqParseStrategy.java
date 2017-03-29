package com.arto.core.annotation.parse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by xiong.j on 2017/3/27.
 */
public interface MqParseStrategy {

    void parseConsumer(Set<String> keySet, Object bean, Method method);

    void parseProducer(Object bean, Field field);

    String getMqType();
}
