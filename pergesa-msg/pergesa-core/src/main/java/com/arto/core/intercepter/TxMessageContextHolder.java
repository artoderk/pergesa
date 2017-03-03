package com.arto.core.intercepter;

import com.arto.event.util.ThreadContextHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 在线程上下文中缓存未提交的事务消息.
 *
 * Created by xiong.j on 2017/3/3.
 */
public class TxMessageContextHolder extends ThreadContextHolder {

    @SuppressWarnings("unchecked")
    public static List<Object> getTxMessages(){
        return (List)getContext();
    }

    @SuppressWarnings("unchecked")
    public static void setTxMessage(Object object){
        if (getContext() == null) {
            init();
        }
        ((List)getContext()).add(object);
    }

    private static synchronized void init(){
        if (getContext() == null) {
            List<Object> list = new ArrayList<Object>();
            setContext(list);
        }
    }
}
