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
package com.arto.core.config;

import com.arto.event.config.ConfigManager;
import com.arto.event.util.PropertiesResolve;
import com.arto.event.util.PropertiesUtil;
import com.arto.event.util.SpringContextHolder;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * Created by xiong.j on 2017/2/8.
 */
@Slf4j
public class MqConfigManager {

    private final PropertiesResolve propertiesResolve;

    private final String defaultFile = "pergesa-core.properties";

    private final Properties properties;

    private MqConfigManager(){
        propertiesResolve = SpringContextHolder.getBean(PropertiesResolve.class);
        properties = init();
    }

    private static class KafkaConfigHolder{
        public static MqConfigManager instance = new MqConfigManager();
    }

    public static MqConfigManager getInstance(){
        return MqConfigManager.KafkaConfigHolder.instance;
    }

    public static int getInt(String name, int value){
        String val = MqConfigManager.getInstance().getValue(name);
        int result = value;
        if (!Strings.isNullOrEmpty(val)) {
            result = Integer.parseInt(val);
        }
        log.info("Load property '" + name + "' = " + result);
        return result;
    }

    public static String getString(String name, String value){
        String result = MqConfigManager.getInstance().getValue(name);
        if (Strings.isNullOrEmpty(result)) {
            result = value;
        }
        log.info("Load property '" + name + "' = " + result);
        return result;
    }

    public static boolean getBoolean(String name, boolean value){
        String val = MqConfigManager.getInstance().getValue(name);
        boolean result = value;
        if (!Strings.isNullOrEmpty(val)) {
            result = Boolean.valueOf(val);
        }
        log.info("Load property '" + name + "' = " + result);
        return result;
    }

    private String getValue(String name){
        String value = null;
        try {
            value = propertiesResolve.getPropertiesValue(name);
            if (Strings.isNullOrEmpty(value)) {
                return properties.getProperty(name);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Failed load property '" + name + "' from spring");
        }
        return value;
    }

    private Properties init(){
        Properties properties = null;
        try {
            properties = PropertiesUtil.loadProperties(ConfigManager.class, defaultFile);
        } catch (Exception e) {
            log.warn("Failed load property file:" + defaultFile);
        }
        return properties;
    }
}
