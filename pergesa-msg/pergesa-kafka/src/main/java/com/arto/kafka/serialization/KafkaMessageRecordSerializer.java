package com.arto.kafka.serialization;

import com.alibaba.fastjson.JSON;
import com.arto.kafka.common.KMessageRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by xiong.jie on 17/1/18.
 */
public class KafkaMessageRecordSerializer implements Serializer<KMessageRecord> {

    private String encoding = "UTF8";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

        String propertyName = isKey ? "key.serializer.encoding" : "value.serializer.encoding";
        Object encodingValue = configs.get(propertyName);

        if(encodingValue == null) {
            encodingValue = configs.get("serializer.encoding");
        }

        if(encodingValue != null && encodingValue instanceof String) {
            this.encoding = (String)encodingValue;
        }
    }

    @Override
    public byte[] serialize(String topic, KMessageRecord message) {
        try {
            return message == null ? null : JSON.toJSON(message).toString().getBytes(this.encoding);
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException("Error when serializing 'KMessageRecord' to byte[] due to unsupported encoding " + this.encoding);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }
}
