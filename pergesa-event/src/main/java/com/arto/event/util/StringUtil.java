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

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Iterator;
/**
 * Created by xiong.j on 2017/1/25.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {

    public static String join(Collection var0, String var1) {
        StringBuilder var2 = new StringBuilder();

        for(Iterator var3 = var0.iterator(); var3.hasNext(); var2.append(String.valueOf(var3.next()))) {
            if(var2.length() != 0) {
                var2.append(var1);
            }
        }

        return var2.toString();
    }

    public static String checkSize(Object obj, int maxSize) throws Exception {
        if (obj == null) return "";

        String value = "";
        if (obj instanceof String) {
            value = (String)obj;
        } else {
            value = toJsonString(obj);
        }

        if (Strings.isNullOrEmpty(value) || maxSize <= 0) {
            return value;
        }

        if (value.getBytes("utf-8").length <= maxSize) {
            return value;
        }
        throw new Exception("String size over max size. string:" + value + ", max size:" + maxSize);
    }

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String remove(String target, String[] replacement) {
        String result = target;
        for (String val : replacement) {
            result = target.replace(val, "");
        }
        return result;
    }
}
