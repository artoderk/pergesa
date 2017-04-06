package com.arto.event.serialization;

import java.lang.reflect.Type;

/**
 * Created by xiong.j on 2017/4/6.
 */
public interface Serializer {

    String serializer(Object object) throws Exception;

    Object deserializer(Type type, String value) throws Exception;
}
