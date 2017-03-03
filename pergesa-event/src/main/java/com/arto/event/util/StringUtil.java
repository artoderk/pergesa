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
        StringBuffer var2 = new StringBuffer();

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
            value = JSON.toJSONString(obj);
        }

        if (Strings.isNullOrEmpty(value)) return "";

        if (value.getBytes("utf-8").length <= maxSize) {
            return value;
        }
        throw new Exception("String size over max size. string:" + value + ", max size:" + maxSize);
    }
}
