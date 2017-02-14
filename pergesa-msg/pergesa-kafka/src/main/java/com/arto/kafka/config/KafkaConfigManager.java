package com.arto.kafka.config;

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
public class KafkaConfigManager {

    private final PropertiesResolve propertiesResolve;

    private final String defaultFile = "pergesa-kafka.properties";

    private final Properties properties;

    private KafkaConfigManager(){
        propertiesResolve = SpringContextHolder.getBean(PropertiesResolve.class);
        properties = init();
    }

    private static class KafkaConfigHolder{
        public static KafkaConfigManager instance = new KafkaConfigManager();
    }

    public static KafkaConfigManager getInstance(){
        return KafkaConfigManager.KafkaConfigHolder.instance;
    }

    public static int getInt(String name, int value){
        String val = KafkaConfigManager.getInstance().getValue(name);
        int result = value;
        if (!Strings.isNullOrEmpty(val)) {
            result = Integer.parseInt(val);
        }
        log.info("Load property '" + name + "' = " + result);
        return result;
    }

    public static String getString(String name, String value){
        String result = KafkaConfigManager.getInstance().getValue(name);
        if (Strings.isNullOrEmpty(result)) {
            result = value;
        }
        log.info("Load property '" + name + "' = " + result);
        return result;
    }

    public static boolean getBoolean(String name, boolean value){
        String val = KafkaConfigManager.getInstance().getValue(name);
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
        } catch (IllegalArgumentException e) {
            log.debug("Failed load property '" + name + "' from spring");
        }

        if (Strings.isNullOrEmpty(value)) {
            return properties.getProperty(name);
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
