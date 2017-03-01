package com.arto.event.util;

import com.arto.event.common.Destroyable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiong.j on 2017/2/22.
 */
@Slf4j
@Component
public class SpringDestroyableUtil {

    public SpringDestroyableUtil(){
        log.info("SpringDestroyableUtil init.");
    }

    private static Map<String, Destroyable> destroyableMap = new HashMap<String, Destroyable>();

    public static void add(String key, Destroyable object){
        destroyableMap.put(key, object);
        log.info("Hook destroyable object: " + key);
    }

    @PreDestroy
    public void destroy(){
        for(Map.Entry<String, Destroyable> entry : destroyableMap.entrySet()){
            entry.getValue().destroy();
            log.info("Destroy object: " + entry.getKey());
        }
    }

}
