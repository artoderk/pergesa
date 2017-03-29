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
package com.arto.amq.config;

import com.arto.event.config.ConfigManager;
import com.arto.event.util.PropertiesResolve;
import com.arto.event.util.PropertiesUtil;
import com.arto.event.util.SpringContextHolder;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * Created by xiong.j on 2017/3/21.
 */
@Slf4j
public class AmqConfigManager {

    private final PropertiesResolve propertiesResolve;

    private final String defaultFile = "pergesa-activemq.properties";

    private final Properties properties;

    private AmqConfigManager(){
        propertiesResolve = SpringContextHolder.getBean(PropertiesResolve.class);
        properties = init();
    }

    private static class AmqConfigHolder{
        public static AmqConfigManager instance = new AmqConfigManager();
    }

    public static AmqConfigManager getInstance(){
        return AmqConfigManager.AmqConfigHolder.instance;
    }

    public static int getInt(String name, int value){
        String val = AmqConfigManager.getInstance().getValue(name);
        int result = value;
        if (!Strings.isNullOrEmpty(val)) {
            result = Integer.parseInt(val);
        }
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    public static String getString(String name, String value){
        String result = AmqConfigManager.getInstance().getValue(name);
        if (Strings.isNullOrEmpty(result)) {
            result = value;
        }
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    public static boolean getBoolean(String name, boolean value){
        String val = AmqConfigManager.getInstance().getValue(name);
        boolean result = value;
        if (!Strings.isNullOrEmpty(val)) {
            result = Boolean.valueOf(val);
        }
        log.debug("Load property '" + name + "' = " + result);
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
