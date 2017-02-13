package com.arto.event.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xiong.j on 2017/2/8.
 */
public class PropertiesUtil {

    public static Properties loadProperties(Class cls, String file) throws Exception {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = cls.getClassLoader().getResourceAsStream(file);
            properties.load(in);
        } catch (Exception e) {
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        return properties;
    }
}
