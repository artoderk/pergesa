package com.arto.event.util;

/**
 * Created by yangzz on 16/7/21.
 */
public abstract class ThreadContextHolder {

    private final static ThreadLocal<Object> ctxHolder= new ThreadLocal<Object>();

    public static Object getContext(){
        return ctxHolder.get();
    }

    public static void setContext(Object object){
        ctxHolder.set(object);
    }

    public static void clear(){
        ctxHolder.remove();
    }
}
