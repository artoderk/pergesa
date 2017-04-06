package com.arto.event.serialization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.arto.event.util.StringUtil;

import java.lang.reflect.Type;

/**
 * Created by xiong.j on 2017/4/6.
 */
public class JsonSerializer implements Serializer{

    private final PropertyFilter filter;

    private final int maxSize;

    public JsonSerializer() {
        this(null, -1);
    }

    public JsonSerializer(PropertyFilter filter, int maxSize) {
        this.maxSize = maxSize;
        this.filter = filter;
    }

    @Override
    public String serializer(Object object) throws Exception {
        return StringUtil.checkSize(JSON.toJSONString(object, filter), maxSize);
    }

    @Override
    public Object deserializer(Type type, String value) throws Exception {
        return JSON.parseObject(value, type);
    }
}
