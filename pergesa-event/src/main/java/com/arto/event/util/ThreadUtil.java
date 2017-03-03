package com.arto.event.util;

import org.slf4j.Logger;

/**
 * Created by xiong.j on 2017/1/18.
 */
public class ThreadUtil {

    public static void sleep(long millis) {
        sleep(millis, null);
    }

    public static void sleep(long millis, Logger log) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            if (log != null) {
                log.warn("Thread interrupted.", e);
            }
        }
    }
}
