package com.arto.kafka.serialization;

import com.alibaba.fastjson.JSON;
import com.arto.kafka.common.KMessageRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by xiong.jie on 17/1/18.
 */
public class KMessageRecordDeserializer implements Deserializer<KMessageRecord> {

    private String encoding = "UTF8";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

        String propertyName = isKey ? "key.deserializer.encoding" : "value.deserializer.encoding";
        Object encodingValue = configs.get(propertyName);

        if(encodingValue == null) {
            encodingValue = configs.get("deserializer.encoding");
        }

        if(encodingValue != null && encodingValue instanceof String) {
            this.encoding = (String)encodingValue;
        }
    }

    @Override
    public KMessageRecord deserialize(String topic, byte[] bytes) {
        try {
            return bytes == null ? null : JSON.parseObject(new String(bytes, encoding),KMessageRecord.class);
        } catch (UnsupportedEncodingException var4) {
            throw new SerializationException("Error when deserializing byte[] to 'KMessageRecord' due to unsupported encoding " + this.encoding);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }
}
