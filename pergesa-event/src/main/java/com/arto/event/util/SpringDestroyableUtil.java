package com.arto.event.util;

import com.arto.event.common.Destroyable;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiong.j on 2017/2/22.
 */
@Component
public class SpringDestroyableUtil {

    private static Map<String, Destroyable> destroyableMap = new HashMap<String, Destroyable>();

    public static void add(String key, Destroyable object){
        destroyableMap.put(key, object);
    }

    @PreDestroy
    public void destroy(){
        for(Map.Entry<String, Destroyable> entry : destroyableMap.entrySet()){
            entry.getValue().destroy();
        }
    }
}
