package com.arto.event.util;

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
}
