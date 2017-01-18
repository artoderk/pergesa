package com.arto.event.util;

import org.slf4j.Logger;

/**
 * Created by xiong.j on 2017/1/18.
 */
public class ThreadUtil {

    public static void sleep(long millis, Thread thread, Logger log) {
        try {
            thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Consumer thread interrupted.", e);
        }
    }
}
