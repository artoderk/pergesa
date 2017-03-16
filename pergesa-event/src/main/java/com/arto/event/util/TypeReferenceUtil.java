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

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 参照com.alibaba.fastjson.TypeReference
 *
 * Created by xiong.j on 2017/1/25.
 */
public class TypeReferenceUtil {

    private static final String defaultMethodName = "onMessage";

    private static ConcurrentMap<String, SoftReference<Type>> types = new ConcurrentHashMap<String, SoftReference<Type>>();

    public static Type getType(Object obj){
        return getType(obj, defaultMethodName);
    }

    public static Type getType(Object obj, String methodName){
        if (types.containsKey(methodName)) {
            if (types.get(methodName).get() != null) {
                return types.get(methodName).get();
            }
        }
        return createType(obj, methodName);
    }

    private static synchronized Type createType(Object obj, String methodName) {
        if (types.containsKey(methodName)) {
            if (types.get(methodName).get() != null) {
                return types.get(methodName).get();
            }
        }

        Method method = getMethod(obj, methodName);
        Type type = method.getGenericParameterTypes()[0];
        types.put(methodName, new SoftReference<Type>(type));
        return type;
    }

    private static Method getMethod(Object obj, String methodName){
        Method[] methods = obj.getClass().getMethods();
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                method = m;
            }
        }
        return method;
    }

}
