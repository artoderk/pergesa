package com.arto.event.exception;

/**
 * Created by xiong.j on 2016/7/21
 */
public class EventProcessException extends RuntimeException {

    public EventProcessException(String message) {
        super(message);
    }

    public EventProcessException(Throwable e) {
        super(e);
    }

    public EventProcessException(String message, Throwable e){
        super(message,e);
    }
}
