package com.arto.amq.util;

/**
 * Created by xiong.j on 2017/3/29.
 */
public class AmqStringUtil {

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

}
