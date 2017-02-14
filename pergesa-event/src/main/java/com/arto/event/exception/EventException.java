package com.arto.event.exception;

/**
 * Created by xiong.j on 2016/7/21
 */
public class EventException extends RuntimeException {

    public EventException(String message) {
        super(message);
    }

    public EventException(Throwable e) {
        super(e);
    }

    public EventException(String message, Throwable e){
        super(message,e);
    }
}
