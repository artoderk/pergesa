package com.arto.amq.util;

import com.arto.core.exception.MqClientException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by xiong.j on 2017/3/29.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmqUtil {

    public static String getDestName(String dest){
        if (destHasParam(dest)) {
            return dest.substring(0, dest.indexOf("?"));
        }
        return dest;
    }

    public static boolean destHasParam(String dest) {
        return dest.contains("?");
    }

    public static String addParamToDest(String dest, String params) {
        String destUrl = "";
        if (destHasParam(dest)) {
            destUrl = dest + "&" + params;
        } else {
            destUrl = dest + "?" + params;
        }
        return destUrl;
    }

    public static int convert2AmqPriority(int msgPriority) {
        return msgPriority * 2;
    }

    public static int convert2MsgPriority(int amqPriority) {
        return amqPriority / 2;
    }

    public static boolean isPubSubDomain(String dest){
        if (dest.startsWith("T") || dest.startsWith("t")) {
            return true;
        } else if (dest.startsWith("Q") || dest.startsWith("q")) {
            return false;
        } else {
            throw new MqClientException("Activemq's destination name must start with 'T' or 'Q'. destination:" + dest);
        }
    }
}
