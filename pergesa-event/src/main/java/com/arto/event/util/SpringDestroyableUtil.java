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
