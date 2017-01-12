package com.arto.event.storage;

import com.sohu.idcenter.IdWorker;

/**
 * Created by xiong.j on 2016/7/22.
 */
public class UniqueIdGenerator {
    private final static UniqueIdGenerator UNIQUE_ID_GENERATOR = new UniqueIdGenerator();

    private IdWorker idWorker;

    private UniqueIdGenerator() {
        getIdWorker();
    }

    private IdWorker getIdWorker() {
        long idepo = System.currentTimeMillis() - 3600 * 1000L;
        idWorker = new IdWorker(idepo);
        return idWorker;
    }

    public static long next() {
        return UNIQUE_ID_GENERATOR.idWorker.getId();
    }

    private static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    public static void main(String args[]){
        System.out.println(UniqueIdGenerator.next());

    }
}
