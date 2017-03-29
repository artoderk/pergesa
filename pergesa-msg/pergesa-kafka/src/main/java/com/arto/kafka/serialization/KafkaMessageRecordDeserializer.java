/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.arto.kafka.serialization;

import com.alibaba.fastjson.JSON;
import com.arto.kafka.common.KafkaMessageRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by xiong.jie on 17/1/18.
 */
public class KafkaMessageRecordDeserializer implements Deserializer<KafkaMessageRecord> {

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
    public KafkaMessageRecord deserialize(String topic, byte[] bytes) {
        try {
            return bytes == null ? null : JSON.parseObject(new String(bytes, encoding),KafkaMessageRecord.class);
        } catch (UnsupportedEncodingException var4) {
            throw new SerializationException("Error when deserializing byte[] to 'KafkaMessageRecord' due to unsupported encoding " + this.encoding);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }
}
